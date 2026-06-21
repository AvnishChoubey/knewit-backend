package com.knewit.backend.post.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotePostRequest {

    private String voteType;
}