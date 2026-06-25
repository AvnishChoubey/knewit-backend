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
public class AttachmentMessageDto {

    private Long id;
    private Long conversationId;
    private String senderUsername;
    private String attachmentUrl;
    private String fileName;
    private MessageType messageType;
    private String sentAt;
}