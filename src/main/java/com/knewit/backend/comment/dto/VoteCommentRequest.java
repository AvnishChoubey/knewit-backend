package com.knewit.backend.comment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteCommentRequest {

    private String voteType;
}