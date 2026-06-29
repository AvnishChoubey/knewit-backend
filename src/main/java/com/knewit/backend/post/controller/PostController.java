package com.knewit.backend.post.controller;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.post.dto.CreatePostRequest;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.post.dto.UpdatePostRequest;
import com.knewit.backend.post.dto.VotePostRequest;
import com.knewit.backend.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    @Autowired
    private  PostService postService;

    @PostMapping(value = "/create",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPost(@AuthenticationPrincipal CustomUserDetails customUserDetails, @ModelAttribute CreatePostRequest request) {

        return ResponseEntity.ok(
                postService.createPost(
                        customUserDetails,
                        request
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdatePostRequest request
    ) {

        return ResponseEntity.ok(
                postService.updatePost(
                        postId,
                        customUserDetails,
                        request
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        postService.deletePost(
                postId,
                customUserDetails
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/vote")
    public ResponseEntity<PostDto> votePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody VotePostRequest request
    ) {

        return ResponseEntity.ok(
                postService.votePost(
                        postId,
                        customUserDetails,
                        request
                )
        );
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(required = false) Long viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "HOT") String sort,
            @RequestParam(defaultValue = "HOME") String feedType,
            @RequestParam(required = false) Long cursor
    ) {

        return ResponseEntity.ok(
                postService.getFeed(
                        viewerId,
                        page,
                        size,
                        sort,
                        feedType,
                        cursor
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto>> searchPosts(@RequestParam String keyword,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.searchPosts(keyword, customUserDetails, page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.ok(postService.getPost(customUserDetails, postId));
    }
}