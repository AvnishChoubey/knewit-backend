package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {
    private String id;
    private String title;
    private String name;
    private String body;
    private String subreddit;
    private String authorUsername;
    private String postStatus;
}
