package persistence

import com.typesafe.config.Config
import faunadb.FaunaClient
import faunadb.errors.NotFoundException
import faunadb.query.{Class, Obj, _}
import faunadb.values.{Decoder, _}
import model.Entity

import scala.concurrent.{ExecutionContext, Future}

trait FaunaRepository[A <: Entity] extends Repository[A] with FaunaRepository.Implicits {

  // Note: using Scala's global ExecutionContext for now.
  // Evaluate the use of a a dedicated ExecutionContext for
  // the persistence layer (bulkheading) in a prod environment.
  implicit protected val ec: ExecutionContext = ExecutionContext.global

  protected val client: FaunaClient

  protected val className: String
  protected val classIndexName: String

  implicit protected val codec: Codec[A]

  def nextId(): Future[String] = {
    val result: Future[Value] = client.query(
      NewId()
    )

    result.decode[String]
  }

  def nextIds(size: Int): Future[Seq[String]] = {
    val indexes = (1 to size).toList

    val result: Future[Value] = client.query(
      Map(
        indexes,
        Lambda(_ => NewId())
      )
    )

    result.decode[Seq[String]]
  }

  override def save(entity: A): Future[A] = {
    val result: Future[Value] = client.query(
      saveQuery(entity.id, entity)
    )
    result.decode[A]
  }

  override def saveAll(entities: A*): Future[Seq[A]] = {
    val result: Future[Value] = client.query(
      Map(
        entities,
        Lambda { nextEntity =>
          val id = Select("id", nextEntity)
          saveQuery(id, nextEntity)
        }
      )
    )

    result.decode[Seq[A]]
  }

  override def remove(id: String): Future[Option[A]] = {
    val result: Future[Value] = client.query(
      Select(
        "data",
        Delete(Ref(Class(className), id))
      )
    )

    result.optDecode[A]
  }

  override def find(id: String): Future[Option[A]] = {
    val result: Future[Value] = client.query(
      Select("data", Get(Ref(Class(className), id)))
    )

    result.optDecode[A]
  }

  def findAll(): Future[Seq[A]] = {
    val result: Future[Value] = client.query(
      SelectAll(
        "data",
        Map(
          Paginate(Match(Index(classIndexName))),
          Lambda(nextRef => Select("data", Get(nextRef)))
        )
      )
    )

    result.decode[Seq[A]]
  }

  protected def saveQuery(id: Expr, data: Expr): Expr =
    Select(
      "data",
      If(
        Exists(Ref(Class(className), id)),
        Replace(Ref(Class(className), id), Obj("data" -> data)),
        Create(Ref(Class(className), id), Obj("data" -> data))
      )
    )

}

object FaunaRepository {

  trait Implicits {

    implicit class ExtendedFutureValue(value: Future[Value]) {
      def decode[A: Decoder](implicit ec: ExecutionContext): Future[A] = value.map(_.to[A].get)

      def optDecode[A: Decoder](implicit ec: ExecutionContext): Future[Option[A]] =
        value
          .decode[A]
          .map(Some(_))
          .recover {
            case _: NotFoundException => None
          }
    }
  }

  object Implicits extends Implicits
}

// Settings
class FaunaSettings(config: Config) {
  private val faunaConfig = config.getConfig("fauna-db")
  val endpoint = faunaConfig.getString("endpoint")
  val secret = faunaConfig.getString("secret")
}
