package com.faunadb.persistence;

import com.faunadb.client.query.Pagination;
import com.faunadb.model.Post;
import com.faunadb.model.common.Page;
import com.faunadb.model.common.PaginationOptions;
import com.faunadb.persistence.common.FaunaRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

import static com.faunadb.client.query.Language.Class;
import static com.faunadb.client.query.Language.*;

@Repository
public class PostRepository extends FaunaRepository<Post> {

    public PostRepository() {
        super(Post.class, "posts", "all_posts");
    }

    //-- Custom repository operations specific to the current entity go below --//
    public CompletableFuture<Page<Post>> findByTitle(String title, PaginationOptions po) {
        Pagination paginationQuery = Paginate(Match(Index(Value("posts_by_title")), Value(title)));
        po.getSize().ifPresent(size -> paginationQuery.size(size));
        po.getAfter().ifPresent(after -> paginationQuery.after(Ref(Class(className), Value(after))));
        po.getBefore().ifPresent(before -> paginationQuery.before(Ref(Class(className), Value(before))));

        CompletableFuture<Page<Post>> result =
            client.query(
                Map(
                    paginationQuery,
                    Lambda(Value("nextRef"), Select(Value("data"), Get(Var("nextRef"))))
                )
            )
            .thenApply(this::toPage);

        return result;
    }

}
