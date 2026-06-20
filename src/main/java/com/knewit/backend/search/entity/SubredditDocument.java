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
    private String name; // slug
    private String title; // display title
    private String iconUrl;
    private String visibility;
}
