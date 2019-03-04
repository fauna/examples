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
  * Note that this class does not mean to model Fauna's Page
  * object, but to be a database agnostic page abstraction.
  *
  * One of the main differences with Fauna's Page is that the
  * latter offers a broader flexibility when it comes to the definition
  * of which elements can be contained within the data and cursors fields.
  * Those elements, which are defined during the creation of the Index
  * from which the Page is being derived, has been constrained in this
  * example in order to keep a simple design. Nonetheless, be aware that
  * Fauna's Indexes and Pages can be used to build more complex results.
  *
  * @param data the data within the current sequence
  * @param before the before cursor – if any, it contains the Id of the previous record before the current sequence of data
  * @param after the after cursor – if any, it contains the Id of the next record after the current sequence of data
  * @tparam A the type of data within the sequence
  *
  * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/types.html#page Page]]
  * @see [[https://docs.fauna.com/fauna/current/reference/queryapi/write/createindex Index]]
  */
case class Page[A](data: Seq[A], before: Option[String], after: Option[String])

/**
  * It defines parameters for looking up a result using pagination.
  *
  * @param size the max number of elements to return in the requested Page
  * @param before the before cursor – if any, it indicates to return the previous Page of results before this Id (exclusive)
  * @param after the after cursor – if any, it indicates to return the next Page of results after this Id (inclusive)
  */
case class PaginationOptions(size: Option[Int], before: Option[String], after: Option[String])
