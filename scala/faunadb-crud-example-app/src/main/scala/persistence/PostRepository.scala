package persistence

import faunadb.FaunaClient
import faunadb.query.{Map, _}
import faunadb.values.{Codec, Value}
import model.{Page, PaginationOptions, Post}

import scala.concurrent.Future

class PostRepository(faunaClient: FaunaClient) extends FaunaRepository[Post] {
  override protected val client: FaunaClient = faunaClient
  override protected val className: String = "posts"
  override protected val classIndexName: String = "all_posts"
  implicit override protected val codec: Codec[Post] = Codec.Record[Post]

  //-- Custom repository operations specific to the current entity go below --//

  def findByTitle(title: String)(implicit po: PaginationOptions): Future[Page[Post]] = {
    val beforeCursor = po.before.map(id => Before(Ref(Class(className), id)))
    val afterCursor = po.after.map(id => After(Ref(Class(className), id)))
    val cursor = beforeCursor.orElse(afterCursor).getOrElse(NoCursor)

    val result: Future[Value] =
      client.query(
        Map(
          Paginate(Match(Index("posts_by_title"), title), size = po.size, cursor = cursor),
          Lambda(nextRef => Select("data", Get(nextRef)))
        )
      )

    result.decode[Page[Post]]

  }

}
