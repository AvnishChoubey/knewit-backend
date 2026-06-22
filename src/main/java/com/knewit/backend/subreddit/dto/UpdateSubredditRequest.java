package com.knewit.backend.subreddit.dto;

import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.enums.PostingPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubredditRequest {

    private String title;

    private String description;

    private Topic topic;

    private PostingPolicy postingPolicy;
}
