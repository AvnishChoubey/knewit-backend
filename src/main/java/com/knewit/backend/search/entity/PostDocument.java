package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String body;

    @Field(type = FieldType.Text)
    private String subreddit;

    @Field(type = FieldType.Text)
    private String authorUsername;

    @Field(type = FieldType.Keyword)
    private String postStatus;
}
