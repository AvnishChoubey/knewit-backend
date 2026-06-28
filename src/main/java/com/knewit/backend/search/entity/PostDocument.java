package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostDocument {
    private String id;
    private String title;
    private String body;
    private String subreddit;
    private String authorUsername;
    private String postStatus;
    private String visibility;
}