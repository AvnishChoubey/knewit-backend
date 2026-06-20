package com.knewit.backend.subreddit.response;

import com.knewit.backend.subreddit.enums.Topic;
import com.knewit.backend.subreddit.enums.Visibility;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class SubredditResponse {

    private Long id;

    private String name;

    private String description;

    private String bannerUrl;

    private String iconUrl;

    private String createdBy;

    private Long memberCount;

    private Visibility visibility;

    private Topic topic;
}