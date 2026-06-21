package com.knewit.backend.chat.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.chat.dto.ChatMessageDto;
import com.knewit.backend.chat.entity.ChatConversation;
import com.knewit.backend.chat.entity.ChatMessage;
import com.knewit.backend.chat.enums.MessageType;
import com.knewit.backend.chat.repository.ChatConversationRepository;
import com.knewit.backend.chat.repository.ChatMessageRepository;
import com.knewit.backend.chat.repository.ChatParticipantRepository;
import com.knewit.backend.common.exception.KnewitException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long userId, Long conversationId) {
        // Authorize participant
        participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() ->
                        new KnewitException(
                                "UNAUTHORIZED_CHAT_ACCESS",
                                "You are not a participant of this conversation",
                                HttpStatus.FORBIDDEN
                        )
                );

        return messageRepository.findAllByConversationIdOrderBySentAtAsc(conversationId).stream()
                .map(m -> ChatMessageDto.builder()
                        .id(m.getId())
                        .conversationId(m.getConversation().getId())
                        .senderUsername(m.getSender().getUsername())
                        .body(m.getBody())
                        .messageType(m.getMessageType())
                        .attachmentUrl(m.getAttachmentUrl())
                        .sentAt(m.getSentAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDto sendMessage(Long senderId, Long conversationId, String body) {
        if (body == null || body.isBlank()) {
            throw new KnewitException(
                    "INVALID_MESSAGE",
                    "Message cannot be empty",
                    HttpStatus.BAD_REQUEST
            );
        }
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new KnewitException(
                            "CHAT_NOT_FOUND",
                            "Conversation not found",
                            HttpStatus.NOT_FOUND
                        )
                );

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new KnewitException(
                            "USER_NOT_FOUND",
                            "Sender user not found",
                            HttpStatus.NOT_FOUND
                        )
                );

        participantRepository.findByConversationIdAndUserId(conversationId, senderId)
                .orElseThrow(() ->
                        new KnewitException(
                                "UNAUTHORIZED_CHAT_ACCESS",
                                "You are not a participant of this conversation",
                                HttpStatus.FORBIDDEN
                        )
                );
        LocalDateTime now = LocalDateTime.now();

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .body(body.trim())
                .messageType(MessageType.TEXT)
                .sentAt(now)
                .build();
        messageRepository.save(message);git

        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        ChatMessageDto dto = ChatMessageDto.builder()
                .id(message.getId())
                .conversationId(conversation.getId())
                .senderUsername(sender.getUsername())
                .body(message.getBody())
                .messageType(message.getMessageType())
                .attachmentUrl(message.getAttachmentUrl())
                .sentAt(message.getSentAt().toString())
                .build();

        /*
         * Broadcast to all users subscribed to this chat
         */
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return dto;
    }

    // getUserConversations()
    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(Long userId) {

        return participantRepository.findAllByUserId(userId).stream()
                .map(participant -> {ChatConversation conversation = participant.getConversation();
                    return ConversationDto.builder()
                            .conversationId(conversation.getId())
                            .title(conversation.getTitle())
                            .unreadCount(participant.getUnreadCount())
                            .lastMessageAt(conversation.getLastMessageAt())
                            .build();
                })
                .toList();
    }
}
