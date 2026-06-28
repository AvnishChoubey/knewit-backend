package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "subreddits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubredditDocument {
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String topic;

    @Field(type = FieldType.Keyword)
    private String visibility;
}
