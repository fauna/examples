package rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import model.CreateReplacePostData
import services.PostService

class PostEndpoint(postService: PostService) extends RestEndpoint {

  def createPost: Route =
    (pathEndOrSingleSlash & post & entity(as[CreateReplacePostData])) { data =>
      onSuccess(postService.createPost(data)) { post =>
        complete(StatusCodes.Created, post)
      }
    }

  def createSeveralPosts: Route =
    (pathEndOrSingleSlash & post & entity(as[Seq[CreateReplacePostData]])) { data =>
      onSuccess(postService.createSeveralPosts(data)) { post =>
        complete(StatusCodes.Created, post)
      }
    }

  def retrievePost: Route =
    (path(Segment) & get) { postId =>
      onSuccess(postService.retrievePost(postId)) {
        case Some(message) => complete(StatusCodes.OK, message)
        case None          => complete(StatusCodes.NotFound)
      }
    }

  def retrievePosts: Route =
    (pathEndOrSingleSlash & get) {
      onSuccess(postService.retrievePosts) { posts =>
        complete(StatusCodes.OK, posts)
      }
    }

  def retrievePostsByTitle: Route =
    (pathEndOrSingleSlash & get) {
      parameter("title") { title =>
        onSuccess(postService.retrievePostsByTitle(title)) { posts =>
          complete(StatusCodes.OK, posts)
        }
      }
    }

  def replacePost: Route =
    (path(Segment) & put & entity(as[CreateReplacePostData])) { (id, data) =>
      onSuccess(postService.replacePost(id, data)) {
        case Some(post) => complete(StatusCodes.OK, post)
        case None       => complete(StatusCodes.NotFound)
      }
    }

  def deletePost: Route =
    (path(Segment) & delete) { postId =>
      onSuccess(postService.deletePost(postId)) {
        case Some(post) => complete(StatusCodes.OK, post)
        case _          => complete(StatusCodes.NotFound)
      }
    }

  override def routes: Route =
    pathPrefix("posts") {
      createPost ~
      createSeveralPosts ~
      retrievePost ~
      retrievePostsByTitle ~
      retrievePosts ~
      replacePost ~
      deletePost
    }

}
