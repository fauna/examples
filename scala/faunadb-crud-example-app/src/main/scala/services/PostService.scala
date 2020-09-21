package services

import model.{CreateReplacePostData, Page, PaginationOptions, Post}
import persistence.PostRepository

import scala.concurrent.Future

/**
  * Domain Service for the [[model.Post Post]] entity.
  */
class PostService(repository: PostRepository) extends Service {

  /**
    * It builds up a new [[model.Post Post]] entity with the
    * given [[model.CreateReplacePostData CreateReplacePostData]]
    * and a generated valid Id. Then, it saves the new entity into
    * the repository.
    *
    * @param data the data to create the new Post entity
    * @return the new created Post entity
    */
  def createPost(data: CreateReplacePostData): Future[Post] = {
    // Save
    val result =
      for {
        id <- repository.nextId()
        post <- repository.save(Post(id, data.title, data.tags))
      } yield post

    result
  }

  /**
    * It builds up a several [[model.Post Post]] entities with the
    * given [[model.CreateReplacePostData CreateReplacePostData]] objects
    * and generated valid Ids. Then, it saves all of the new entities into
    * the repository.
    *
    * @param data the data to create the new Post entities
    * @return a sequence of the new created Post entities
    */
  def createSeveralPosts(createUpdateData: Seq[CreateReplacePostData]): Future[Seq[Post]] = {
    def buildEntities(ids: Seq[String]): Future[Seq[Post]] = Future.successful {
      (ids zip createUpdateData) map {
        case (id, data) =>
          Post(id, data.title, data.tags)
      }
    }

    // Save
    val result =
      for {
        ids <- repository.nextIds(createUpdateData.size)
        entities <- buildEntities(ids)
        result <- repository.saveAll(entities: _*)
      } yield result

    result
  }

  /**
    * It retrieves a [[model.Post Post]] by its Id from the repository.
    *
    * @param id the Id of the [[model.Post Post]] to retrieve
    * @return an [[scala.Option Option]] result with the requested [[model.Post Post]] if any
    */
  def retrievePost(id: String): Future[Option[Post]] =
    repository.find(id)

  /**
    * It retrieves a [[model.Page Page]] of [[model.Post Post]] entities from
    * the repository for the given [[model.PaginationOptions PaginationOptions]].
    *
    * @param po the [[model.PaginationOptions PaginationOptions]] to determine which [[model.Page Page]] of results to return
    * @return a [[model.Page Page]] of [[model.Post Post]] entities
    */
  def retrievePosts()(implicit po: PaginationOptions): Future[Page[Post]] =
    repository.findAll()

  /**
    * It retrieves a [[model.Page Page]] of [[model.Post Post]]
    * entities from the repository matching the given title.
    *
    * @param title title to find Posts by
    * @param po the [[model.PaginationOptions PaginationOptions]] to determine which [[model.Page Page]] of results to return
    * @return a [[model.Page Page]] of [[model.Post Post]] entities
    */
  def retrievePostsByTitle(title: String)(implicit po: PaginationOptions): Future[Page[Post]] =
    repository.findByTitle(title)

  /**
    * It looks up a [[model.Post Post]] for the given Id and replaces it
    * with the given [[model.CreateReplacePostData CreateReplacePostData]] if any.
    *
    * @param id the Id of the Post to replace
    * @param data the data to replace the Post with
    * @return an [[scala.Option Option]] result with the replaced [[model.Post Post]] if any
    */
  def replacePost(id: String, data: CreateReplacePostData): Future[Option[Post]] = {
    def replace() = {
      val postEntity = Post(id, data.title, data.tags)
      repository.save(postEntity)
    }

    val result =
      retrievePost(id) flatMap {
        case Some(_) => replace().map(Some(_))
        case None    => Future.successful(None)
      }

    result
  }

  /**
    * It deletes a [[model.Post Post]] from the repository for the given Id.
    *
    * @param id the Id of the [[model.Post Post]] to delete
    * @return an [[scala.Option Option]] result with the deleted [[model.Post Post]] if any
    */
  def deletePost(id: String): Future[Option[Post]] =
    repository.remove(id)

}
