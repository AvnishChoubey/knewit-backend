package com.knewit.backend.subreddit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinRequestDto {

    private Long userId;

    private String username;

    private String memberStatus;

    private String requestedAt;
}