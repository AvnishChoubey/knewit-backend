package com.knewit.backend.comment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CommentResponse {

    private Long id;

    private String content;

    private String author;

    private Long postId;

    private Long parentCommentId;

    private Boolean edited;

    private LocalDateTime createdAt;

    private List<CommentResponse> replies;

}