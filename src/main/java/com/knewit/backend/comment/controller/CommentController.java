package com.knewit.backend.comment.controller;

import com.knewit.backend.comment.request.CreateCommentRequest;
import com.knewit.backend.comment.request.UpdateCommentRequest;
import com.knewit.backend.comment.response.CommentResponse;
import com.knewit.backend.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @RequestParam Long userId,
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
        return commentService.createComment(
                userId,
                postId,
                request
        );
    }

    @PostMapping("/comments/{commentId}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse replyToComment(
            @RequestParam Long userId,
            @PathVariable Long commentId,
            @RequestBody CreateCommentRequest request
    ) {
        return commentService.replyToComment(
                userId,
                commentId,
                request
        );
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> getCommentsByPost(
            @PathVariable Long postId
    ) {
        return commentService.getCommentsByPost(postId);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return commentService.updateComment(
                commentId,
                request
        );
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);
    }
}