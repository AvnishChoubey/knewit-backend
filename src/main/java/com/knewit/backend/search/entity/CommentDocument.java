package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDocument {
    private String id;

    @Field(type = FieldType.Text)
    private String body;

    private String postId;

    @Field(type = FieldType.Text)
    private String authorUsername;

    private String contentStatus;
}
