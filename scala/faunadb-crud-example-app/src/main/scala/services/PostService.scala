package services

import model.{CreateReplacePostData, Post}
import persistence.PostRepository

import scala.concurrent.Future

class PostService(repository: PostRepository) extends Service {

  def createPost(data: CreateReplacePostData): Future[Post] = {
    // Save
    val result: Future[Post] =
      for {
        id <- repository.nextId()
        post <- repository.save(Post(id, data.title, data.tags))
      } yield post

    result
  }

  def createSeveralPosts(createUpdateData: Seq[CreateReplacePostData]): Future[Seq[Post]] = {
    def buildEntities(ids: Seq[String]): Future[Seq[Post]] = Future.successful {
      (ids zip createUpdateData) map {
        case (id, data) =>
          Post(id, data.title, data.tags)
      }
    }

    // Save
    val result: Future[Seq[Post]] =
      for {
        ids <- repository.nextIds(createUpdateData.size)
        entities <- buildEntities(ids)
        result <- repository.saveAll(entities: _*)
      } yield result

    result
  }

  def retrievePost(id: String): Future[Option[Post]] =
    repository.find(id)

  def retrievePosts(): Future[Seq[Post]] =
    repository.findAll()

  def retrievePostsByTitle(title: String): Future[Seq[Post]] =
    repository.findByTitle(title)

  def replacePost(id: String, data: CreateReplacePostData): Future[Option[Post]] = {
    def replace(): Future[Post] = {
      val postEntity = Post(id, data.title, data.tags)
      repository.save(postEntity)
    }

    val result: Future[Option[Post]] =
      retrievePost(id) flatMap {
        case Some(_) => replace().map(Some(_))
        case None    => Future.successful(None)
      }

    result
  }

  def deletePost(id: String): Future[Option[Post]] =
    repository.remove(id)

}
