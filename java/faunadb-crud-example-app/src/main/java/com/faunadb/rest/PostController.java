package com.faunadb.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faunadb.model.CreateReplacePostData;
import com.faunadb.model.Post;
import com.faunadb.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for the {@link Post} entity.
 */
@RestController
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/posts")
    public CompletableFuture<ResponseEntity> createPost(HttpEntity<String> httpEntity) throws IOException {
        String requestBody = httpEntity.getBody();

        // Create single Post
        if(isCreateReplacePostData(requestBody)) {
            CreateReplacePostData data = deserializeCreateReplacePostData(requestBody);
            CompletableFuture<ResponseEntity> result =
                postService.createPost(data)
                    .thenApply(post -> new ResponseEntity(post, HttpStatus.OK));
            return result;
        }

        // Create several Posts
        if(isCreateReplacePostDataList(requestBody)) {
            List<CreateReplacePostData> data = deserializeCreateReplacePostDataList(requestBody);
            CompletableFuture<ResponseEntity> result =
                postService.createSeveralPosts(data)
                    .thenApply(post -> new ResponseEntity(post, HttpStatus.OK));
            return result;
        }

        return CompletableFuture.completedFuture(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/posts/{id}")
    public CompletableFuture<ResponseEntity> retrievePost(@PathVariable("id") String id) {
        CompletableFuture<ResponseEntity> result =
            postService.retrievePost(id)
                .thenApply(optionalPost ->
                    optionalPost
                        .map(post -> new ResponseEntity(post, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND))
            );
        return result;
    }

    @GetMapping("/posts")
    public CompletableFuture<List<Post>> retrievePosts() {
        CompletableFuture<List<Post>> result = postService.retrievePosts();
        return result;
    }

    @GetMapping(value = "/posts", params = {"title"})
    public CompletableFuture<List<Post>> retrievePostsByTitle(@RequestParam("title") String title) {
        CompletableFuture<List<Post>> result = postService.retrievePostsByTitle(title);
        return result;
    }

    @PutMapping(value = "/posts/{id}")
    public CompletableFuture<ResponseEntity> replacePost(@PathVariable("id") String id, @RequestBody CreateReplacePostData data) {
        CompletableFuture<ResponseEntity> result =
            postService.replacePost(id, data)
                .thenApply(optionalPost ->
                    optionalPost
                        .map(post -> new ResponseEntity(post, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND))
                );
        return result;
    }

    @DeleteMapping(value = "/posts/{id}")
    public CompletableFuture<ResponseEntity> deletePost(@PathVariable("id")String id) {
        CompletableFuture<ResponseEntity> result =
            postService.deletePost(id)
                .thenApply(optionalPost ->
                    optionalPost
                        .map(post -> new ResponseEntity(post, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND))
                    );
        return result;
    }

    /**
     * It verifies if the given JSON payload can be
     * deserialized into a {@link CreateReplacePostData} object.
     *
     * @param json the JSON payload to verify
     * @return true if the given JSON payload can be deserialize into a a {@link CreateReplacePostData} object, false if not
     * @throws IOException if there's any error with the given JSON payload
     */
    private Boolean isCreateReplacePostData(String json) throws IOException {
        try {
            objectMapper.readValue(json, CreateReplacePostData.class);
            return true;
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        }
    }

    /**
     * It verifies if the given JSON payload can be
     * deserialized into a {@link List} of {@link CreateReplacePostData} object.
     *
     * @param json the JSON payload to verify
     * @return true if the given JSON payload can be deserialize into a {@link List} of {@link CreateReplacePostData} objects, false if not
     * @throws IOException if there's any error with the given JSON payload
     */
    private Boolean isCreateReplacePostDataList(String json) throws IOException {
        try {
            objectMapper.readValue(json, new TypeReference<List<CreateReplacePostData>>(){});
            return true;
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        }
    }

    /**
     * It deserializes the given JSON payload into a {@link CreateReplacePostData} object.
     *
     * @param json the JSON payload to deserialize
     * @return a {@link CreateReplacePostData} deserialized from the given JSON payload
     * @throws IOException if there's any error with the given JSON payload
     */
    private CreateReplacePostData deserializeCreateReplacePostData(String json) throws IOException {
        return objectMapper.readValue(json, CreateReplacePostData.class);
    }

    /**
     * It deserializes the given JSON payload into a {@link List} of {@link CreateReplacePostData} objects.
     *
     * @param json the JSON payload to deserialize
     * @return a {@link List} of {@link CreateReplacePostData} objects deserialized from the given JSON payload
     * @throws IOException if there's any error with the given JSON payload
     */
    private List<CreateReplacePostData> deserializeCreateReplacePostDataList(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<List<CreateReplacePostData>>(){});
    }
}
