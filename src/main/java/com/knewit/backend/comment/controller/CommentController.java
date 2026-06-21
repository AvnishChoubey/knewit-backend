package com.knewit.backend.comment.controller;

import com.knewit.backend.comment.dto.CommentDto;
import com.knewit.backend.comment.dto.CreateCommentRequest;
import com.knewit.backend.comment.dto.UpdateCommentRequest;
import com.knewit.backend.comment.dto.VoteCommentRequest;
import com.knewit.backend.comment.service.CommentService;
import com.knewit.backend.common.enums.VoteType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @RequestParam Long authorId,
            @RequestBody CreateCommentRequest request
    ) {

        return ResponseEntity.ok(
                commentService.createComment(
                        authorId,
                        postId,
                        request
                )
        );
    }


    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {

        commentService.deleteComment(
                commentId,
                userId
        );

        return ResponseEntity.ok(
                "Comment deleted successfully"
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody UpdateCommentRequest request
    ) {

        return ResponseEntity.ok(
                commentService.updateComment(
                        commentId,
                        userId,
                        request
                )
        );
    }
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<String> voteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody VoteCommentRequest request
    ) {

        commentService.voteComment(
                userId,
                commentId,
                VoteType.valueOf(
                        request.getVoteType()
                                .toUpperCase()
                )
        );

        return ResponseEntity.ok(
                "Vote updated successfully"
        );
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestParam Long viewerId
    ) {

        return ResponseEntity.ok(
                commentService.getCommentsForPost(
                        postId,
                        viewerId
                )
        );
    }
}