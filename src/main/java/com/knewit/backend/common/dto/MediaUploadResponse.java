package com.knewit.backend.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaUploadResponse {

    private String url;

    private String publicId;
}