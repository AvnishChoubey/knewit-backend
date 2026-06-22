package com.knewit.backend.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaUploadResponse {

    private String url;

    private String publicId;
}