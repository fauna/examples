package com.faunadb.persistence.common;

import com.faunadb.model.Post;
import com.faunadb.model.common.Entity;
import com.faunadb.model.common.Page;
import com.faunadb.model.common.PaginationOptions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Base trait for implementing Repositories.</p>
 *
 * <p>It defines a set of base methods mimicking a collection API.</p>
 *
 * <p>Note that unlike DAOs, which are designed following a data access
 * orientation, Repositories are implemented following a collection
 * orientation. This means that instead of exposing a set of CRUD
 * operations for a data model, a Repository interface will resemble
 * the one of a Collection for a domain model.</p>
 *
 */
public interface Repository<T extends Entity> {

    /**
     * <p>It saves the given Entity into the Repository.</p>
     *
     * <p>If an Entity with the same Id already exists in
     * the Repository, it will be replaced with the one
     * supplied.</p>
     *
     * @param entity the Entity to be saved
     * @return the saved Entity
     */
    CompletableFuture<T> save(T entity);

    /**
     * <p>It saves all the given Entities into the Repository.</p>
     *
     * <p>If an Entity with the same Id already exists in
     * the Repository for any of the given Entities, it
     * will be replaced with the one supplied.</p>
     *
     * @param entities the Entities to be saved
     * @return the saved Entities
     */
    CompletableFuture<List<T>> saveAll(List<T> entities);

    /**
     * It finds an Entity for the given Id.
     *
     * @param id the Id of the Entity to be found
     * @return the Entity if found or an empty result if not
     */
    CompletableFuture<Optional<T>> find(String id);

    /**
     * It retrieves a {@link Page} of {@link Post} entities
     * for the given {@link PaginationOptions}.
     *
     * @param po the {@link PaginationOptions} to determine which {@link Page} of results to return
     * @return a {@link Page} of Entities
     */
    CompletableFuture<Page<T>> findAll(PaginationOptions po);

    /**
     * It finds the Entity for the given Id and
     * removes it. If no Entity can be found for
     * the given Id an empty result is returned.
     *
     * @param id the Id of the Entity to be removed
     * @return the removed Entity if found or an empty result if not
     */
    CompletableFuture<Optional<T>> remove(String id);
}
