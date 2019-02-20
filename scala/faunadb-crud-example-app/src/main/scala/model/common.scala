package model

/**
  * Base trait for implementing Domain Entities.
  *
  * An Entity is defined by its unique Identity.
  */
trait Entity {
  val id: String
}

/**
  * It represents a sequence of elements along with
  * cursors indicators to move backwards and forwards.
  *
  * @param data the data within the current sequence
  * @param before the before cursor – if any, it contains the Id of the previous record before the current sequence
  * @param after the after cursor – if any, it contains the Id of the next record after the current sequence
  * @tparam A the type of data within the sequence
  */
case class Page[A](data: Seq[A], before: Option[String], after: Option[String])

/**
  * It defines parameters for looking up a result using pagination.
  *
  *
  * @param size the size of the requested results
  * @param before the before cursor – if any, it indicates the requested result should
  * @param after the after cursor – if any, it contains the Id of the next record after the current sequence
  */
case class PaginationOptions(size: Option[Int], before: Option[String], after: Option[String])
