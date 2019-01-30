import com.typesafe.config.ConfigFactory
import faunadb.FaunaClient
import persistence.{FaunaSettings, PostRepository}
import rest.{PostEndpoint, RestServer}
import services.PostService

trait PersistenceModule {
  lazy val faunaSettings = new FaunaSettings(ConfigFactory.load())
  implicit lazy val faunaClient = FaunaClient(faunaSettings.secret, faunaSettings.endpoint)

  lazy val postRepository = new PostRepository(faunaClient)
}

trait DomainModule { self: PersistenceModule =>
  lazy val postService = new PostService(postRepository)
}

trait RestModule { self: DomainModule with PersistenceModule =>
  lazy val endpoints = Seq(
    new PostEndpoint(postService)
  )

  RestServer.start(endpoints)
}

object Main extends App with PersistenceModule with DomainModule with RestModule
