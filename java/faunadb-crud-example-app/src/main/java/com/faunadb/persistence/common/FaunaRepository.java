package com.faunadb.persistence.common;

import com.faunadb.client.FaunaClient;
import com.faunadb.client.errors.NotFoundException;
import com.faunadb.client.query.Expr;
import com.faunadb.client.types.Value;
import com.faunadb.model.common.Entity;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.Class;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.faunadb.client.query.Language.Class;
import static com.faunadb.client.query.Language.*;

/**
 * Base Repository implementation backed by FaunaDB.
 *
 * @param <T> a valid Entity type
 *
 * @see <a href="https://fauna.com/">FaunaDB</a>
 */
public abstract class FaunaRepository<T extends Entity> implements Repository<T>, IdentityFactory {

    @Autowired
    protected FaunaClient client;

    protected final Class<T> entityType;
    protected final String className;
    protected final String classIndexName;

    public FaunaRepository(Class<T> entityType, String className, String classIndexName) {
        this.entityType = entityType;
        this.className = className;
        this.classIndexName = classIndexName;
    }

    /**
     * <p>It returns a unique valid Id leveraging Fauna's NewId function.</p>
     *
     * <p>The produced Id is guaranteed to be unique across the entire
     * cluster and once generated will never be generated a second time.</p>
     *
     * @return a unique valid Id
     *
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/misc/newid">NewId</a>
     */
    @Override
    public CompletableFuture<String> nextId() {
        CompletableFuture<String> result =
            client.query(
                NewId()
            )
            .thenApply(value -> value.to(String.class).get());

        return result;
    }

    /**
     * <p>It returns a List of unique valid Ids leveraging Fauna's NewId function.</p>
     *
     * <p>The produced Ids are guaranteed to be unique across the entire
     * cluster and once generated will never be generated a second time.</p>
     *
     * @param size the number of unique Ids to return
     * @return a List of unique valid Ids
     *
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/misc/newid">NewId</a>
     */
    @Override
    public CompletableFuture<List<String>> nextIds(int size) {
        List<Integer> indexes = IntStream
            .range(0, size)
            .mapToObj(i -> i)
            .collect(Collectors.toList());


        CompletableFuture<List<String>> result =
            client.query(
                Map(
                    Value(indexes),
                    Lambda(Value("i"), NewId())
                )
            )
            .thenApply(value -> value.asCollectionOf(String.class).get().stream().collect(Collectors.toList()));

        return result;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/create">Create</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/replace">Replace</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/basic/if">If</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists">Exists</a>
     */
    @Override
    public CompletableFuture<T> save(T entity) {
        CompletableFuture<T> result =
            client.query(
                saveQuery(Value(entity.getId()), Value(entity))
            )
        .thenApply(this::toEntity);

        return result;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/create">Create</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/replace">Replace</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/basic/if">If</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists">Exists</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/collection/map">Map</a>
     */
    @Override
    public CompletableFuture<List<T>> saveAll(List<T> entities) {
        CompletableFuture<List<T>> result =
            client.query(
                Map(
                    Value(entities),
                    Lambda(
                        Value("entity"),
                        saveQuery(Select(Value("id"), Var("entity")), Var("entity"))
                    )
                )
            )
            .thenApply(this::toList);


        return result;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/delete">Delete</a>
     */
    @Override
    public CompletableFuture<Optional<T>> remove(String id) {
        CompletableFuture<T> result =
            client.query(
                Select(
                    Value("data"),
                    Delete(Ref(Class(className), Value(id)))
                )
            )
            .thenApply(this::toEntity);

        CompletableFuture<Optional<T>> optionalResult = toOptionalResult(result);

        return optionalResult;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/read/get">Get</a>
     */
    @Override
    public CompletableFuture<Optional<T>> find(String id) {
        CompletableFuture<T> result =
            client.query(
                Select(
                    Value("data"),
                    Get(Ref(Class(className), Value(id)))
                )
            )
            .thenApply(this::toEntity);

        CompletableFuture<Optional<T>> optionalResult = toOptionalResult(result);

        return optionalResult;
    }

    /**
     * {@inheritDoc}
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/read/paginate">Paginate</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/collection/map">Map</a>
     */
    @Override
    public CompletableFuture<List<T>> findAll() {
        CompletableFuture<List<T>> result =
            client.query(
                SelectAll(
                    Value("data"),
                    Map(
                        Paginate(Match(Index(Value(classIndexName)))),
                        Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef"))))
                    )
                )
            )
            .thenApply(this::toList);

        return result;
    }

    /**
     * It leverages Fauna Query Language enriched features to build
     * a transactional query for performing a valid {@link Repository#save} operation.
     *
     * @param id the Id of the Entity to be saved
     * @param data the data of the Entity to be saved
     *
     * @return a query for performing a valid {@link Repository#save} operation
     *
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/create">Create</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/write/replace">Replace</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/basic/if">If</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/logical/exists">Exists</a>
     */
    protected Expr saveQuery(Expr id, Expr data) {
        Expr query =
            Select(
                Value("data"),
                If(
                    Exists(Ref(Class(className), id)),
                    Replace(Ref(Class(className), id), Obj("data", data)),
                    Create(Ref(Class(className), id), Obj("data", data))
                )
            );

        return query;
    }

    /**
     * <p>It converts a FaunaDB {@link Value} into an {@link Entity}.</p>
     *
     * <p>In order this to work, the concrete Entity class to be converted
     * must include Fauna's encoding annotations.</p>
     *
     * @param value the Value to convert from
     * @return the converted Entity from the given Value
     *
     * @see <a href="https://github.com/fauna/faunadb-jvm/blob/master/docs/java.md#how-to-work-with-user-defined-classes">Encoding and decoding user defined classes</a>
     */
    protected T toEntity(Value value) {
        return value.to(entityType).get();
    }

    /**
     * <p>It converts a FaunaDB {@link Value} into a {@link List} with {@link Entity} type.</p>
     *
     * <p>In order this to work, the concrete Entity class to be converted
     * must include Fauna's encoding annotations. At the same time, the
     * Value to convert from must be of a Fauna Array type.</p>
     *
     * @param value the Value to convert from
     * @return the converted Entity from the given Value
     *
     * @see <a href="https://github.com/fauna/faunadb-jvm/blob/master/docs/java.md#how-to-work-with-user-defined-classes">Encoding and decoding user defined classes</a>
     * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/types.html#array">Array</a>
     *
     */
    protected List<T> toList(Value value) {
        return value.asCollectionOf(entityType).get().stream().collect(Collectors.toList());
    }

    /**
     * <p>It recovers from a {@link NotFoundException} with an Optional result.</p>
     *
     * <p>If the given {@link CompletableFuture} contains a successful result, it returns
     * a new {@link CompletableFuture} containing the result wrapped in an {@link Optional} instance.</p>
     *
     * <p>If the given {@link CompletableFuture} contains a failing result caused
     * by a {@link NotFoundException}, it returns a new {@link CompletableFuture} containing
     * an empty {@link Optional} instance.</p>
     *
     * <p>If the given {@link CompletableFuture} contains a failing result caused
     * by any other {@link Throwable}, it returns a new {@link CompletableFuture} with
     * the same failing result.</p>
     *
     * @param result the result to transform from
     * @return a new Optional result derived from the original result
     */
    protected CompletableFuture<Optional<T>> toOptionalResult(CompletableFuture<T> result) {
        CompletableFuture<Optional<T>> optionalResult =
            result.handle((v, t) -> {
                CompletableFuture<Optional<T>> r = new CompletableFuture<>();
                if(v != null) r.complete(Optional.of(v));
                else if(t != null && t.getCause() instanceof NotFoundException) r.complete(Optional.empty());
                else r.completeExceptionally(t);
                return r;
            }).thenCompose(Function.identity());

        return optionalResult;
    }

}
