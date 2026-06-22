package com.knewit.backend.subreddit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubredditMemberDto {

    private Long userId;

    private String username;

    private String memberStatus;

    private Boolean isModerator;

    private String joinedAt;
}