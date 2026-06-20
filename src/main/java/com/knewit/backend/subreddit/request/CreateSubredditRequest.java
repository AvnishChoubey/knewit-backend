package com.knewit.backend.subreddit.request;

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