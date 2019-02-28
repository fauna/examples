package com.faunadb.persistence;

import com.faunadb.model.Post;
import com.faunadb.persistence.common.FaunaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.faunadb.client.query.Language.*;

/**
 * {@link FaunaRepository} implementation for the {@link Post} entity.
 */
@Repository
public class PostRepository extends FaunaRepository<Post> {

    public PostRepository() {
        super(Post.class, "posts", "all_posts");
    }

    //-- Custom repository operations specific to the current entity go below --//

    /**
     * It finds all Posts matching the given title.
     *
     * @param title the title to find Posts by
     * @return a list of Posts matching the given title
     *
     * @see <a href="https://app.fauna.com/documentation/reference/queryapi#paginate">Paginate</a>
     * @see <a href="https://app.fauna.com/documentation/reference/queryapi#map">Map</a>
     */
    public CompletableFuture<List<Post>> findByTitle(String title) {
        CompletableFuture<List<Post>> result =
            client.query(
                SelectAll(
                    Value("data"),
                    Map(
                        Paginate(Match(Index(Value("posts_by_title")), Value(title))),
                        Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef"))))
                    )
                )
            )
            .thenApply(this::toList);

        return result;
    }

}
