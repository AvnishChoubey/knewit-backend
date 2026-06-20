package com.knewit.backend.comment.transformer;

import com.mountblue.knewit.comments.controller.entity.Comment;
import com.mountblue.knewit.comments.controller.response.CommentResponse;

import java.util.List;

public class CommentTransformer {

    private CommentTransformer() {
    }

    public static CommentResponse toResponse(
            Comment comment
    ) {

        List<CommentResponse> replies = comment.getReplies()
                .stream()
                .filter(reply -> !Boolean.TRUE.equals(reply.getDeleted()))
                .map(CommentTransformer::toResponse)
                .toList();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getUsername())
                .postId(comment.getPost().getId())
                .parentCommentId(
                        comment.getParentComment() != null
                                ? comment.getParentComment().getId()
                                : null
                )
                .edited(comment.getEdited())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}