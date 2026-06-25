package com.knewit.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipantDto {

    private Long userId;
    private String username;
    private String avatarUrl;
    private Boolean isMuted;
    private String avatarPublicId;
}