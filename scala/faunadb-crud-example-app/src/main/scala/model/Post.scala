package model

case class Post(id: String, title: String, tags: Seq[String]) extends Entity

case class CreateReplacePostData(title: String, tags: Seq[String])
