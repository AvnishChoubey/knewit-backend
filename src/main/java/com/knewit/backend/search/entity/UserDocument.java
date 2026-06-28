package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {
    private String id;

    @Field(type = FieldType.Text)
    private String username;
}
