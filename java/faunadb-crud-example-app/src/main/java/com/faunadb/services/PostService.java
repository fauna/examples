package com.faunadb.services;

import com.faunadb.model.CreateReplacePostData;
import com.faunadb.model.Post;
import com.faunadb.model.common.Page;
import com.faunadb.model.common.PaginationOptions;
import com.faunadb.persistence.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Domain Service for the {@link Post} entity.
 */
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    /**
     * It builds up a new {@link Post} entity with the
     * given {@link CreateReplacePostData} and a generated
     * valid Id. Then, it saves the new entity into the repository.
     *
     * @param data the data to create the new Post entity
     * @return the new created Post entity
     */
    public CompletableFuture<Post> createPost(CreateReplacePostData data) {
        CompletableFuture<Post> result =
            postRepository.nextId()
                .thenApply(id -> new Post(id, data.getTitle(), data.getTags()))
                .thenCompose(post -> postRepository.save(post));

        return result;
    }

    /**
     * It builds up a several {@link Post} entities with the
     * given {@link CreateReplacePostData} objects and generated
     * valid Ids. Then, it saves all of the new entities into
     * the repository.
     *
     * @param data the data to create the new Post entities
     * @return a list of the new created Post entities
     */
    public CompletableFuture<List<Post>> createSeveralPosts(List<CreateReplacePostData> data) {
        CompletableFuture<List<Post>> result =
            postRepository.nextIds(data.size())
                .thenApply(ids ->
                    IntStream
                        .range(0, data.size())
                        .mapToObj(i -> new Post(ids.get(i), data.get(i).getTitle(), data.get(i).getTags()))
                        .collect(Collectors.toList()))
                .thenCompose(posts ->
                    postRepository.saveAll(posts));

        return result;
    }

    /**
     * It retrieves a {@link Post} by its Id from the repository.
     *
     * @param id the Id of the Post to retrieve
     * @return an Optional result with the requested Post if any
     */
    public CompletableFuture<Optional<Post>> retrievePost(String id) {
        return postRepository.find(id);
    }

    /**
     * It retrieves a {@link Page} of {@link Post} entities from
     * the repository for the given {@link PaginationOptions}.
     *
     * @param po the {@link PaginationOptions} to determine which {@link Page} of results to return
     * @return a {@link Page} of Entities
     */
    public CompletableFuture<Page<Post>> retrievePosts(PaginationOptions po) {
        return postRepository.findAll(po);
    }

    /**
     * It retrieves a {@link Page} of {@link Post} entities
     * from the repository matching the given title.
     *
     * @param title title to find Posts by
     * @param po the {@link PaginationOptions} to determine which {@link Page} of results to return
     * @return a {@link Page} of {@link Post} entities
     */
    public CompletableFuture<Page<Post>> retrievePostsByTitle(String title, PaginationOptions po){
        return postRepository.findByTitle(title, po);
    }

    /**
     * It looks up a {@link Post} for the given Id and replaces it
     * with the given {@link CreateReplacePostData} if any.
     *
     * @param id the Id of the Post to replace
     * @param data the data to replace the Post with
     * @return an Optional result with the replaced Post if any
     */
    public CompletableFuture<Optional<Post>> replacePost(String id, CreateReplacePostData data) {
        CompletableFuture<Optional<Post>> result =
            postRepository.find(id)
                .thenCompose(optionalPost ->
                    optionalPost
                        .map(post -> postRepository.save(new Post(id, data.getTitle(), data.getTags())).thenApply(Optional::of))
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));

        return result;
    }

    /**
     * It deletes a {@link Post} from the repository for the given Id.
     *
     * @param id the Id of the Post to delete
     * @return an Optional result with the deleted Post if any
     */
    public CompletableFuture<Optional<Post>> deletePost(String id) {
        return postRepository.remove(id);
    }

}
