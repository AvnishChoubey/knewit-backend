package com.knewit.backend.subreddit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanMemberRequest {

    private Long moderatorId;

    private String username;

    private String reason;
}