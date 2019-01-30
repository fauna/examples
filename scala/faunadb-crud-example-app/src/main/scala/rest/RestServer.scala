package rest

import java.time.Instant

import akka.actor.{ActorSystem, CoordinatedShutdown, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import faunadb.FaunaClient

import scala.util.{Failure, Success, Try}

/**
  * RestServer backed by Akka HTTP. Alla Akka references are encapsulated here.
  */
object RestServer {

  private object BindFailure extends CoordinatedShutdown.Reason

  def start(restEndpoints: Seq[RestEndpoint] = Seq.empty)(implicit faunaClient: FaunaClient): Unit = {
    implicit val system = ActorSystem("rest-server")
    implicit val ec = system.dispatcher
    implicit val mat = ActorMaterializer()
    val log = system.log

    val settings = RestServerSettings(system)
    val (host, port) = (settings.host, settings.port)
    val routes = restEndpoints.map(_.routes).reduce(_ ~ _)
    val filteredRoutes = FaunaFilter.filter(routes)

    val shutdown = CoordinatedShutdown(system)

    Http()
      .bindAndHandle(filteredRoutes, host, port)
      .onComplete {
        case Failure(error) =>
          log.error(error, "Shutting down, because cannot bind to {}:{}!", host, port)
          shutdown.run(BindFailure)

        case Success(binding) =>
          log.info("Listening for HTTP connections on {}", binding.localAddress)
          shutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, "http-server.unbind") { () =>
            binding.unbind()
          }
      }
  }

}

// Settings
class RestServerSettings(config: Config) extends Extension {
  private val restServerConfig = config.getConfig("rest-server")
  val host = restServerConfig.getString("host")
  val port = restServerConfig.getInt("port")
}

object RestServerSettings extends ExtensionId[RestServerSettings] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): RestServerSettings =
    new RestServerSettings(system.settings.config)

  override def lookup(): ExtensionId[_ <: Extension] = RestServerSettings

}

// Filters
object FaunaFilter {
  def filter(routes: Route)(implicit faunaClient: FaunaClient): Route =
    // Extract lastXtnTime from Request
    optionalHeaderValueByName("X-Last-Txn-Time") { lastXtnTimeHeaderValue =>
      lastXtnTimeHeaderValue
        .flatMap(toLong)
        .foreach { lastTxnTime =>
          // TODO: call syncLastTxnTime when new driver is released
          // faunaClient.syncLastTxnTime(lastSeenXtn)
        }
      // Put back updated lastXtnTime into Response
      mapResponseHeaders { responseHeaders =>
        // TODO: call lastTxnTime when new driver is released
        // val = faunaClient.lastTxnTime(lastSeenXtn)
        val lastTxnTime = Instant.now().toEpochMilli
        responseHeaders :+ RawHeader("X-Last-Txn-Time", lastTxnTime.toString)
      } {
        routes
      }
    }

  private def toLong(value: String): Option[Long] = Try(value.toLong).toOption
}
