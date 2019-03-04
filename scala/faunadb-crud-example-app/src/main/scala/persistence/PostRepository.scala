package persistence

import faunadb.FaunaClient
import faunadb.query.{Map, _}
import faunadb.values.Codec
import model.{Page, PaginationOptions, Post}

import scala.concurrent.Future

/**
  * [[persistence.FaunaRepository FaunaRepository]] implementation for the [[model.Post Post]] entity.
  */
class PostRepository(faunaClient: FaunaClient) extends FaunaRepository[Post] {
  override protected val client: FaunaClient = faunaClient
  override protected val className = "posts"
  override protected val classIndexName = "all_posts"
  implicit override protected val codec: Codec[Post] = Codec.Record[Post]

  //-- Custom repository operations specific to the current entity go below --//

  /**
    * It finds all Posts matching the given title.
    *
    * @param title title to find Posts by
    * @param po the [[model.PaginationOptions PaginationOptions]] to determine which [[model.Page Page]] of results to return
    * @return a [[model.Page Page]] of [[model.Post Post]] entities
    */
  def findByTitle(title: String)(implicit po: PaginationOptions): Future[Page[Post]] = {
    val beforeCursor = po.before.map(id => Before(Ref(Class(className), id)))
    val afterCursor = po.after.map(id => After(Ref(Class(className), id)))
    val cursor = beforeCursor.orElse(afterCursor).getOrElse(NoCursor)

    val result =
      client.query(
        Map(
          Paginate(Match(Index("posts_by_title"), title), size = po.size, cursor = cursor),
          Lambda(nextRef => Select("data", Get(nextRef)))
        )
      )

    result.decode[Page[Post]]

  }

}
