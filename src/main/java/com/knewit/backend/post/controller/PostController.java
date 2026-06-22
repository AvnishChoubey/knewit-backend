package com.knewit.backend.post.controller;

import com.knewit.backend.post.dto.CreatePostRequest;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.post.dto.UpdatePostRequest;
import com.knewit.backend.post.dto.VotePostRequest;
import com.knewit.backend.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PostDto> createPost(
            @RequestParam Long authorId,
            @ModelAttribute CreatePostRequest request
    ) {

        return ResponseEntity.ok(
                postService.createPost(
                        authorId,
                        request
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @RequestParam Long authorId,
            @RequestBody UpdatePostRequest request
    ) {

        return ResponseEntity.ok(
                postService.updatePost(
                        postId,
                        authorId,
                        request
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam Long authorId
    ) {

        postService.deletePost(
                postId,
                authorId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/vote")
    public ResponseEntity<PostDto> votePost(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestBody VotePostRequest request
    ) {

        return ResponseEntity.ok(
                postService.votePost(
                        postId,
                        userId,
                        request
                )
        );
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam Long viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "HOT") String sort
    ) {

        return ResponseEntity.ok(
                postService.getFeed(
                        viewerId,
                        page,
                        size,
                        sort
                )
        );
    }

    @GetMapping("/saved")
    public ResponseEntity<List<PostDto>> getSavedPosts(
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                postService.getSavedPosts(userId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam Long viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                postService.getUserPosts(
                        userId,
                        viewerId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam Long viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                postService.searchPosts(
                        keyword,
                        viewerId,
                        page,
                        size
                )
        );
    }



    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(
            @PathVariable Long postId,
            @RequestParam Long viewerId
    ) {
        return ResponseEntity.ok(
                postService.getPost(postId, viewerId)
        );
    }


    @PostMapping("/{postId}/save")
    public ResponseEntity<Boolean> toggleSavePost(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                postService.toggleSavePost(
                        postId,
                        userId
                )
        );
    }
    @GetMapping("/pending")
    public ResponseEntity<Page<PostDto>> getPendingPosts(
            @RequestParam Long subredditId,
            @RequestParam Long moderatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                postService.getPendingPosts(
                        subredditId,
                        moderatorId,
                        page,
                        size
                )
        );
    }

    @PatchMapping("/{postId}/approve")
    public ResponseEntity<PostDto> approvePost(
            @PathVariable Long postId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                postService.approvePost(
                        postId,
                        moderatorId
                )
        );
    }

    @PostMapping("/{postId}/follow")
    public ResponseEntity<Boolean> toggleFollowPost(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                postService.toggleFollowPost(
                        postId,
                        userId
                )
        );
    }

    @GetMapping("/following")
    public ResponseEntity<List<PostDto>> getFollowedPosts(
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                postService.getFollowedPosts(
                        userId
                )
        );
    }

    @PatchMapping("/{postId}/reject")
    public ResponseEntity<PostDto> rejectPost(
            @PathVariable Long postId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                postService.rejectPost(
                        postId,
                        moderatorId
                )
        );
    }

    @GetMapping("/subreddit/{subredditName}")
    public ResponseEntity<Page<PostDto>> getPostsBySubreddit(
            @PathVariable String subredditName,
            @RequestParam Long viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                postService.getPostsBySubreddit(
                        subredditName,
                        viewerId,
                        page,
                        size
                )
        );
    }
}