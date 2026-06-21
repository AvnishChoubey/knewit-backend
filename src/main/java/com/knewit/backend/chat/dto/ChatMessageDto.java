package com.knewit.backend.chat.dto;

import com.knewit.backend.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private Long id;
    private Long conversationId;
    private String senderUsername;
    private String body;
    private MessageType messageType;
    private String attachmentUrl;
    private String sentAt;
}