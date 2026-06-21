package com.knewit.backend.comment.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    private Long postId;

    private String authorUsername;

    private Long parentCommentId;

    private Integer depthLevel;

    private String body;

    private String commentStatus;

    private Long upvoteCount;

    private Long downvoteCount;

    private Long shareCount;

    private String createdAt;

    private String votedState;
}