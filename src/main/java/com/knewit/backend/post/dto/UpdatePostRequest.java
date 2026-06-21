package com.knewit.backend.post.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

    private String title;

    private String body;

    private String externalUrl;
}