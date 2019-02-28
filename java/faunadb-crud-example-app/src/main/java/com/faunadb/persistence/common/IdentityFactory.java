package com.faunadb.persistence.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>It defines the base operations for implementing an Identity factory.</p>
 *
 * <p>It enables support for early Identity generation and assignment, that is
 * before the Entity is saved into the Repository. This allows to decouple the
 * operations of generating the Id and saving the Entity from each other. This
 * pattern lets among other things to create an Entity with all of its mandatory
 * fields and get rid of inconsistent states, i.e having an Entity with a null Id.</p>
 */
public interface IdentityFactory {

    /**
     * It returns a unique valid Id.
     *
     * @return a unique valid Id
     */
    CompletableFuture<String> nextId();

    /**
     * It returns a List of unique valid Ids with the given size.
     *
     * @param size the number of unique Ids to return
     * @return a List of unique valid Ids
     */
    CompletableFuture<List<String>> nextIds(int size);
}
