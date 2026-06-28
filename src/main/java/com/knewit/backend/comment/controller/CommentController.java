package com.knewit.backend.comment.controller;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.comment.dto.CommentDto;
import com.knewit.backend.comment.dto.CreateCommentRequest;
import com.knewit.backend.comment.dto.UpdateCommentRequest;
import com.knewit.backend.comment.dto.VoteCommentRequest;
import com.knewit.backend.comment.service.CommentService;
import com.knewit.backend.common.enums.VoteType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @PathVariable("postId") Long postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(customUserDetails, postId));
    }

    @PostMapping("/create")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CreateCommentRequest request) {

        return ResponseEntity.ok(
                commentService.createComment(
                        customUserDetails,
                        postId,
                        request
                )
        );
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        commentService.deleteComment( customUserDetails, commentId);

        return ResponseEntity.ok("Comment deleted successfully");
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdateCommentRequest request
    ) {

        return ResponseEntity.ok(commentService.updateComment(customUserDetails, commentId, request));
    }


    @PostMapping("/{commentId}/vote")
    public ResponseEntity<String> voteComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                              @PathVariable Long commentId,
                                              @RequestBody VoteCommentRequest request) {
        commentService.voteComment(customUserDetails, commentId, VoteType.valueOf(request.getVoteType().toUpperCase()));

        return ResponseEntity.ok("Vote updated successfully");
    }
}