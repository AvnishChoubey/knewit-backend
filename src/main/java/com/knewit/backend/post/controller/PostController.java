package com.knewit.backend.post.controller;

import com.mountblue.knewit.post.request.CreatePostRequest;
import com.mountblue.knewit.post.response.PostResponse;
import com.mountblue.knewit.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(
            @RequestParam Long userId,
            @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(
                userId,
                request
        );
    }

    @GetMapping("/{id}")
    public PostResponse getPost(
            @PathVariable Long id
    ) {
        return postService.getPostById(id);
    }

    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/subreddit/{subredditId}")
    public List<PostResponse> getPostsBySubreddit(
            @PathVariable Long subredditId
    ) {
        return postService.getPostsBySubreddit(subredditId);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(
            @PathVariable Long id,
            @RequestBody CreatePostRequest request
    ) {
        return postService.updatePost(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable Long id
    ) {
        postService.deletePost(id);
    }
}