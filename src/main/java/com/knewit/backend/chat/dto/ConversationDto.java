package com.knewit.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDto {

    private Long conversationId;
    private String title;
    private Integer unreadCount;
    private LocalDateTime lastMessageAt;
}
