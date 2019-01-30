package persistence

import model.Entity

import scala.concurrent.Future

/**
  * Base trait for implementing Repositories.
  *
  * It defines a set of base methods mimicking a collection API.
  *
  * Note: unlike DAOs, which are designed following a data access
  * orientation, Repositories are implemented following a collection
  * orientation. The focus should be put on the domain as a model
  * rather than on data and any CRUD operations that may be used behind
  * the scenes to manage its persistence.
  *
  */
trait Repository[A <: Entity] extends IdentityFactory {
  def save(entity: A): Future[A]
  def saveAll(entities: A*): Future[Seq[A]]
  def remove(id: String): Future[Option[A]]
  def find(id: String): Future[Option[A]]
  def findAll(): Future[Seq[A]]
}

/**
  * Base trait for implementing an Identity factory. It enables
  * support for early Identity generation and assignment
  * (i.e. before the Entity is saved into the Repository).
  */
trait IdentityFactory {
  def nextId(): Future[String]
  def nextIds(size: Int): Future[Seq[String]]
}
