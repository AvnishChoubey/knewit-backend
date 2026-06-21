package com.knewit.backend.chat.dto;

import com.knewit.backend.chat.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDto {

    private Long id;
    private ConversationType conversationType;
    private String title;
    private List<String> participantUsernames;
    private String lastMessageAt;
    private String createdAt;
}
