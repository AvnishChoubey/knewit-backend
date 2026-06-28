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

public class CommentDocument {
    private String id;
    private String body;
    private String postId;
    private String authorUsername;
    private String contentStatus;
}
