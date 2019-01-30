package model

/**
  * Base trait for implementing Domain Entities.
  *
  * An Entity is defined by its unique Identity.
  */
trait Entity {
  val id: String
}
