package persistence

import model.{Entity, Page, PaginationOptions}

import scala.concurrent.Future

/**
  * Base trait for implementing Repositories.
  *
  * It defines a set of base methods mimicking a collection API.
  *
  * Note that unlike DAOs, which are designed following a data access
  * orientation, Repositories are implemented following a collection
  * orientation. This means that instead of exposing a set of CRUD
  * operations for a data model, a Repository interface will resemble
  * the one of a Collection for a domain model.
  *
  * @tparam A [[model.Entity Entity]] type the Repository will be modelled after
  */
trait Repository[A <: Entity] extends IdentityFactory {

  /**
    * It saves the given Entity into the Repository.
    *
    * If an Entity with the same Id already exists in
    * the Repository, it will be replaced with the one
    * supplied.
    *
    * @param entity  the Entity to be saved
    * @return the saved Entity
    */
  def save(entity: A): Future[A]

  /**
    * It saves all the given Entities into the Repository.
    *
    * If an Entity with the same Id already exists in
    * the Repository for any of the given Entities, it
    * will be replaced with the one supplied.
    *
    * @param entities the Entities to be saved
    * @return the saved Entities
    */
  def saveAll(entities: A*): Future[Seq[A]]

  /**
    * It finds the Entity for the given Id and
    * removes it. If no Entity can be found for
    * the given Id an empty result is returned.
    *
    * @param id the Id of the Entity to be removed
    * @return the removed Entity if found or an empty result if not
    */
  def remove(id: String): Future[Option[A]]

  /**
    * It finds an Entity for the given Id.
    *
    * @param id the Id of the Entity to be found
    * @return the Entity if found or an empty result if not
    */
  def find(id: String): Future[Option[A]]

  /**
    * It retrieves a [[model.Page Page]] of entities
    * for the given [[model.PaginationOptions PaginationOptions]].
    *
    * @param po the [[model.PaginationOptions PaginationOptions]] to determine which [[model.Page Page]] of results to return
    * @return a [[model.Page Page]] of entities
    */
  def findAll()(implicit po: PaginationOptions): Future[Page[A]]
}

/**
  * It defines the base operations for implementing an Identity factory.
  *
  * It enables support for early Identity generation and assignment, that is
  * before the Entity is saved into the Repository. This allows to decouple the
  * operations of generating the Id and saving the Entity from each other. This
  * pattern lets among other things to create an Entity with all of its mandatory
  * fields and get rid of inconsistent states, i.e having an Entity with a null Id.
  */
trait IdentityFactory {

  /**
    * It returns a unique valid Id.
    *
    * @return a unique valid Id
    */
  def nextId(): Future[String]

  /**
    * It returns a sequence of unique valid Ids with the given size.
    *
    * @param size the number of unique Ids to return
    * @return a sequence of unique valid Ids
    */
  def nextIds(size: Int): Future[Seq[String]]
}
