package com.knewit.backend.post.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long id;

    private String subredditName;

    private String authorUsername;

    private String title;

    private String type;

    private Boolean saved;

    private String body;

    private String mediaUrl;

    private String mediaPublicId;

    private String externalUrl;

    private String postStatus;

    private Long upvoteCount;

    private Long downvoteCount;

    private Long shareCount;

    private Long commentCount;

    private Double scoreHot;

    private Double scoreBest;

    private Double scoreRising;

    private Double scoreTop;

    private String createdAt;

    private String votedState;
}