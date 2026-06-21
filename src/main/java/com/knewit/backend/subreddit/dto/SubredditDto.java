package com.knewit.backend.subreddit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubredditDto {

    private Long id;

    private String name;

    private String title;

    private String description;

    private String visibility;

    private String postingPolicy;

    private String iconUrl;

    private String topic;

    private String iconPublicId;

    private String creatorUsername;

    private Long memberCount;

    private Long postCount;

    private Boolean isArchived;

    private String createdAt;
}