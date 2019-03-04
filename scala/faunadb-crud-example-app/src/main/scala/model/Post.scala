package model

/**
  * A Post entity.
  *
  * It represents a simple Post, like the one in a Blog,
  * along with some basic attributes.
  *
  * @param id the Post Id
  * @param title the Post title
  * @param tags the Post tags
  */
case class Post(id: String, title: String, tags: Seq[String]) extends Entity

/**
  * It contains all the necessary data, with the exception of
  * the Id, for creating or replacing a [[model.Post Post]] entity.
  *
  * @param title the Post title
  * @param tags the Post tags
  */
case class CreateReplacePostData(title: String, tags: Seq[String])
