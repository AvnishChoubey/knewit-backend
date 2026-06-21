package com.knewit.backend.post.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaDto {

    private Long id;

    private String mediaType;

    private String cloudinaryUrl;

    private String cloudinaryPublicId;

    private Long byteSize;

    private Integer width;

    private Integer height;

    private Integer durationSeconds;
}