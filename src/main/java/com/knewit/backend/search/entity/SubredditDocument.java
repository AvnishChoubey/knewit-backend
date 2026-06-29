package com.knewit.backend.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubredditDocument {
    private String id;
    private String name;
    private String title;
    private String topic;
    private String visibility;
}
