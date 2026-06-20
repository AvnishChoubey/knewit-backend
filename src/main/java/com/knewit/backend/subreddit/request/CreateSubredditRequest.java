package com.knewit.backend.subreddit.request;

import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.enums.Visibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSubredditRequest {

    private String name;

    private String description;

    private String bannerUrl;

    private String iconUrl;

    private Visibility visibility;

    private Topic topic;
}