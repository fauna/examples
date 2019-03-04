package persistence

import com.typesafe.config.Config
import faunadb.FaunaClient
import faunadb.errors.NotFoundException
import faunadb.query.{Class, Obj, _}
import faunadb.values.{Decoder, _}
import model.{Entity, Page, PaginationOptions}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Base Repository implementation backed by FaunaDB.
  *
  * @tparam A [[model.Entity Entity]] type the Repository will be modelled after
  * @see [[https://fauna.com/ FaunaDB]]
  */
trait FaunaRepository[A <: Entity] extends Repository[A] with FaunaRepository.Implicits {
  /*
   * Note: using Scala's global ExecutionContext for keeping
   * the example with a simple design. Evaluate the use of a
   * dedicated ExecutionContext for the persistence layer
   * (bulkheading) in a prod environment.
   */
  implicit protected val ec: ExecutionContext = ExecutionContext.global

  protected val client: FaunaClient

  protected val className: String
  protected val classIndexName: String

  implicit protected val codec: Codec[A]

  /**
    * It returns a unique valid Id leveraging Fauna's NewId function.
    *
    * The produced Id is guaranteed to be unique across the entire
    * cluster and once generated will never be generated a second time.
    *
    * @return a unique valid Id
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/misc/newid NewId]]
    */
  def nextId(): Future[String] = {
    val result = client.query(
      NewId()
    )

    result.decode[String]
  }

  /**
    * It returns a sequence of unique valid Ids leveraging Fauna's NewId function.
    *
    * The produced Ids are guaranteed to be unique across the entire
    * cluster and once generated will never be generated a second time.
    *
    * @param size the number of unique Ids to return
    * @return a sequence of unique valid Ids
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/misc/newid NewId]]
    */
  def nextIds(size: Int): Future[Seq[String]] = {
    val indexes = (1 to size).toList

    val result = client.query(
      Map(
        indexes,
        Lambda(_ => NewId())
      )
    )

    result.decode[Seq[String]]
  }

  /**
    * @inheritdoc
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/create Create]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/replace Replace]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/basic/if If]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists Exists]]
    */
  override def save(entity: A): Future[A] = {
    val result = client.query(
      saveQuery(entity.id, entity)
    )
    result.decode[A]
  }

  /**
    * @inheritdoc
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/create Create]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/replace Replace]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/basic/if If]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists Exists]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/collection/map Map]]
    */
  override def saveAll(entities: A*): Future[Seq[A]] = {
    val result = client.query(
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

  /**
    * @inheritdoc
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/delete Delete]]
    */
  override def remove(id: String): Future[Option[A]] = {
    val result = client.query(
      Select(
        "data",
        Delete(Ref(Class(className), id))
      )
    )

    result.optDecode[A]
  }

  /**
    * @inheritdoc
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/read/get Get]]
    */
  override def find(id: String): Future[Option[A]] = {
    val result = client.query(
      Select("data", Get(Ref(Class(className), id)))
    )

    result.optDecode[A]
  }

  /**
    * @inheritdoc
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/read/paginate Paginate]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/collection/map Map]]
    */
  def findAll()(implicit po: PaginationOptions): Future[Page[A]] = {
    val beforeCursor = po.before.map(id => Before(Ref(Class(className), id)))
    val afterCursor = po.after.map(id => After(Ref(Class(className), id)))
    val cursor = beforeCursor.orElse(afterCursor).getOrElse(NoCursor)

    val result = client.query(
      Map(
        Paginate(Match(Index(classIndexName)), size = po.size, cursor = cursor),
        Lambda(nextRef => Select("data", Get(nextRef)))
      )
    )

    result.decode[Page[A]]
  }

  /**
    * It leverages Fauna Query Language enriched features to build
    * a transactional query for performing a valid [[persistence.Repository#save Repository#save]] operation.
    *
    * @param id the Id of the Entity to be saved
    * @param data the data of the Entity to be saved
    * @return a query for performing a valid [[persistence.Repository#save Repository#save]] operation
    *
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/create Create]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/replace Replace]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/basic/if If]]
    * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists Exists]]
    */
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

    /**
      * It returns an implicit [[faunadb.values.Decoder Decoder]] for converting
      * from a [[faunadb.values.Value Value]] into a [[model.Page Page]].
      *
      * In order this to work, there must be an implicit [[faunadb.values.Decoder Decoder]] for the
      * concrete [[model.Page Page]]'s [[model.Entity Entity]] type in scope. At the same time, the [[faunadb.values.Value Value]] to
      * convert from must be of a Fauna Page type.
      *
      * @param decoder [[faunadb.values.Decoder Decoder]] for [[model.Page Page]]'s type parameter
      * @tparam A [[model.Page Page]]'s type parameter
      * @return an implicit [[faunadb.values.Decoder Decoder]] for [[model.Page Page]]
      *
      * @see [[https://github.com/fauna/faunadb-jvm/blob/master/docs/scala.md#how-to-work-with-user-defined-classes Encoding and decoding user defined classes]]
      * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/types.html#page Page]]
      */
    implicit def pageDecoder[A](implicit decoder: Decoder[A]): Decoder[Page[A]] = new Decoder[Page[A]] {
      def decode(v: Value, path: FieldPath): Result[Page[A]] = {
        /*
         * Note that below code for extracting the data within the "after"
         * and the "before" cursors directly depends on the definition of
         * the Index from which the Page is being derived. For this particular
         * case, the Index return values should only contain the Ref field
         * from the Instances being covered by the Index. If the Index return
         * values should contain more fields, update below code accordingly.
         */
        val before = v("before").to[Seq[RefV]].map(_.head.id).toOpt
        val after = v("after").to[Seq[RefV]].map(_.head.id).toOpt

        val result: Result[Page[A]] =
          v("data").to[Seq[A]].map { data =>
            Page(data, before, after)
          }

        result
      }
    }

    implicit class ExtendedFutureValue(value: Future[Value]) {

      /**
        * It converts a [[faunadb.values.Value Value]] into an `Object` of
        * the given [[faunadb.values.Decoder Decoder]]'s type.
        *
        * @tparam A the type to convert to
        * @return the converted `Object` from the given [[faunadb.values.Value]]
        */
      def decode[A: Decoder](implicit ec: ExecutionContext): Future[A] = value.map(_.to[A].get)

      /**
        * It recovers from a [[faunadb.errors.NotFoundException NotFoundException]]
        * with an [[scala.Option Option]] result.
        *
        * If it's a [[scala.util.Success Success]] value, it wraps the value within a [[scala.Some Some]] result.
        *
        * If it's a [[scala.util.Failure Failure]] value caused by a [[faunadb.errors.NotFoundException NotFoundException]],
        * it returns [[scala.None None]] result.
        *
        * @tparam A the type to convert to
        * @return
        */
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

/**
  * Settings class for setting up a [[faunadb.FaunaClient FaunaClient]].
  *
  * @param config base [[com.typesafe.config.Config Config]] for reading the settings from
  */
class FaunaSettings(config: Config) {
  private val faunaConfig = config.getConfig("fauna-db")
  val endpoint = faunaConfig.getString("endpoint")
  val secret = faunaConfig.getString("secret")
}
