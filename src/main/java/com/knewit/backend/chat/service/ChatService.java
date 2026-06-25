package com.knewit.backend.chat.service;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.chat.dto.*;
import com.knewit.backend.chat.entity.ChatConversation;
import com.knewit.backend.chat.entity.ChatMessage;
import com.knewit.backend.chat.entity.ChatParticipant;
import com.knewit.backend.chat.enums.ConversationType;
import com.knewit.backend.chat.enums.MessageType;
import com.knewit.backend.chat.repository.ChatConversationRepository;
import com.knewit.backend.chat.repository.ChatMessageRepository;
import com.knewit.backend.chat.repository.ChatParticipantRepository;
import com.knewit.backend.common.exception.KnewitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(CustomUserDetails customUserDetails, Long conversationId, int page, int size) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();

        ChatParticipant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                        HttpStatus.FORBIDDEN));

        if (participant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this conversation", HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<ChatMessage> messagePage = messageRepository.findByConversationIdAndDeletedAtIsNullOrderBySentAtDesc(
                                conversationId, pageable);

        List<ChatMessageDto> messages = messagePage.getContent().stream()
                        .map(message -> ChatMessageDto.builder()
                                        .id(message.getId())
                                        .conversationId(message.getConversation().getId())
                                        .senderUsername(message.getSender().getUsername())
                                        .body(message.getBody())
                                        .messageType(message.getMessageType())
                                        .attachmentUrl(message.getAttachmentUrl())
                                        .sentAt(message.getSentAt().toString())
                                        .editedAt(message.getEditedAt() == null ? null : message.getEditedAt().toString())
                                        .build())
                        .toList();

        Collections.reverse(messages);

        return messages;
    }

    @Transactional
    public ChatMessageDto sendMessage(CustomUserDetails customUserDetails, Long conversationId, String body) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long senderId = customUserDetails.getUserId();

        if (body == null || body.isBlank()) {
            throw new KnewitException("INVALID_MESSAGE", "Message cannot be empty", HttpStatus.BAD_REQUEST);
        }

        ChatConversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new KnewitException("CHAT_NOT_FOUND", "Conversation not found",
                                        HttpStatus.NOT_FOUND));
        if (conversation.getDeletedAt() != null) {
            throw new KnewitException("CHAT_DELETED", "Conversation no longer exists", HttpStatus.BAD_REQUEST);
        }

        User sender = userRepository.findById(senderId).orElseThrow(() ->
                        new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        ChatParticipant senderParticipant = participantRepository.findByConversationIdAndUserId(conversationId, senderId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                        HttpStatus.FORBIDDEN));

        if (senderParticipant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this conversation", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();

        ChatMessage message = ChatMessage.builder()
                        .conversation(conversation)
                        .sender(sender)
                        .body(body.trim())
                        .messageType(MessageType.TEXT)
                        .sentAt(now)
                        .build();

        messageRepository.save(message);

        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        List<ChatParticipant> participants = participantRepository.findAllByConversationId(conversationId);

        for (ChatParticipant participant : participants) {
            if (participant.getLeftAt() != null) {
                continue;
            }
            if (!participant.getUser().getId().equals(senderId)) {
                participant.setUnreadCount(participant.getUnreadCount() + 1);
            }
        }

        participantRepository.saveAll(participants);

        ChatMessageDto dto = ChatMessageDto.builder()
                        .id(message.getId())
                        .conversationId(conversationId)
                        .senderUsername(sender.getUsername())
                        .body(message.getBody())
                        .messageType(message.getMessageType())
                        .attachmentUrl(message.getAttachmentUrl())
                        .sentAt(message.getSentAt().toString())
                        .build();

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();

        List<ChatParticipant> participations = participantRepository.findByUserIdAndLeftAtIsNull(userId);
        return participations.stream()
                .filter(participant -> participant.getConversation().getDeletedAt() == null)
                .map(participant -> {
                    ChatConversation conversation = participant.getConversation();
                    String title;
                    if (conversation.getConversationType() == ConversationType.DIRECT) {
                        List<ChatParticipant> participants = participantRepository.findAllByConversationId(
                                                conversation.getId());
                        ChatParticipant otherParticipant = participants.stream()
                                .filter(p -> !p.getUser().getId().equals(userId)).findFirst().orElse(null);

                        title = otherParticipant != null ? otherParticipant.getUser().getUsername() : "Unknown User";
                    } else {
                        title = conversation.getTitle();
                    }

                    return ConversationDto.builder()
                            .conversationId(conversation.getId())
                            .title(title)
                            .unreadCount(participant.getUnreadCount())
                            .lastMessageAt(conversation.getLastMessageAt())
                            .build();
                })

                .sorted((a, b) -> {
                    if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) {
                        return 0;
                    }
                    if (a.getLastMessageAt() == null) {
                        return 1;
                    }
                    if (b.getLastMessageAt() == null) {
                        return -1;
                    }
                    return b.getLastMessageAt().compareTo(a.getLastMessageAt());
                })
                .toList();
    }


    @Transactional
    public ConversationDto createDirectConversation(CustomUserDetails customUserDetails, Long targetUserId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long currentUserId = customUserDetails.getUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new KnewitException("INVALID_CHAT", "Cannot create conversation with yourself", HttpStatus.BAD_REQUEST);
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Current user not found", HttpStatus.NOT_FOUND));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Target user not found", HttpStatus.NOT_FOUND));

        List<ChatParticipant> currentUserChats = participantRepository.findByUserIdAndLeftAtIsNull(currentUserId);

        for (ChatParticipant participant : currentUserChats) {
            ChatConversation conversation = participant.getConversation();
            if (conversation.getConversationType() != ConversationType.DIRECT) {
                continue;
            }
            List<ChatParticipant> participants = participantRepository.findAllByConversationId(conversation.getId());

            boolean targetExists = participants.stream().anyMatch(p -> p.getUser().getId().equals(targetUserId));

            if (targetExists && participants.size() == 2) {
                return ConversationDto.builder()
                        .conversationId(conversation.getId())
                        .title(targetUser.getUsername())
                        .unreadCount(0)
                        .lastMessageAt(conversation.getLastMessageAt())
                        .build();
            }
        }

        ChatConversation conversation = ChatConversation.builder()
                        .conversationType(ConversationType.DIRECT)
                        .createdBy(currentUser)
                        .lastMessageAt(null)
                        .build();

        conversationRepository.save(conversation);

        participantRepository.save(ChatParticipant.builder()
                        .conversation(conversation)
                        .user(currentUser)
                        .build()
        );

        participantRepository.save(ChatParticipant.builder()
                        .conversation(conversation)
                        .user(targetUser)
                        .build()
        );

        return ConversationDto.builder()
                .conversationId(conversation.getId())
                .title(targetUser.getUsername())
                .unreadCount(0)
                .lastMessageAt(null)
                .build();
    }

    @Transactional
    public ConversationDto createGroupConversation(CustomUserDetails customUserDetails, CreateGroupConversationDto dto) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long creatorId = customUserDetails.getUserId();

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new KnewitException("INVALID_GROUP_NAME", "Group title cannot be empty", HttpStatus.BAD_REQUEST);
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Creator not found", HttpStatus.NOT_FOUND));

        ChatConversation conversation = ChatConversation.builder()
                        .conversationType(ConversationType.GROUP)
                        .title(dto.getTitle().trim())
                        .createdBy(creator)
                        .build();

        conversationRepository.save(conversation);

        // Add creator
        participantRepository.save(ChatParticipant.builder()
                        .conversation(conversation)
                        .user(creator)
                        .build()
        );

        // Add selected participants
        if (dto.getParticipantIds() != null) {
            for (Long participantId : dto.getParticipantIds()) {
                if (participantId.equals(creatorId)) {
                    continue;
                }
                User participant = userRepository.findById(participantId)
                        .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found : " + participantId,
                                                HttpStatus.NOT_FOUND));

                participantRepository.save(ChatParticipant.builder()
                                .conversation(conversation)
                                .user(participant)
                                .build()
                );
            }
        }

        // Create system message
        ChatMessage systemMessage = ChatMessage.builder()
                                    .conversation(conversation).sender(creator).body(creator.getUsername() + " created the group")
                                    .messageType(MessageType.SYSTEM)
                                    .sentAt(LocalDateTime.now())
                                    .build();

        messageRepository.save(systemMessage);

        conversation.setLastMessageAt(systemMessage.getSentAt());

        conversationRepository.save(conversation);

        return ConversationDto.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .unreadCount(0)
                .lastMessageAt(conversation.getLastMessageAt())
                .build();
    }

    @Transactional
    public String markAsRead(CustomUserDetails customUserDetails, Long conversationId, Long lastMessageId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();

        ChatParticipant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                        HttpStatus.FORBIDDEN));

        if (participant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this conversation", HttpStatus.BAD_REQUEST);
        }

        ChatMessage message = messageRepository.findById(lastMessageId)
                        .orElseThrow(() -> new KnewitException("MESSAGE_NOT_FOUND", "Message not found",
                                HttpStatus.NOT_FOUND));

        if (!message.getConversation().getId().equals(conversationId)) {
            throw new KnewitException("INVALID_MESSAGE", "Message does not belong to this conversation",
                    HttpStatus.BAD_REQUEST
            );
        }

        participant.setLastReadMessageId(message);
        participant.setUnreadCount(0);
        participantRepository.save(participant);

        return "Messages marked as read successfully";
    }

    @Transactional
    public ChatMessageDto editMessage(CustomUserDetails customUserDetails, Long conversationId, Long messageId, String newBody) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();

        if (newBody == null || newBody.isBlank()) {
            throw new KnewitException("INVALID_MESSAGE", "Message cannot be empty", HttpStatus.BAD_REQUEST);
        }

        ChatMessage message = messageRepository.findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new KnewitException("MESSAGE_NOT_FOUND", "Message not found in this chat",
                                        HttpStatus.NOT_FOUND));

        if (message.getDeletedAt() != null) {
            throw new KnewitException("MESSAGE_DELETED", "Message has been deleted", HttpStatus.BAD_REQUEST);
        }

        if (!message.getSender().getId().equals(userId)) {
            throw new KnewitException("UNAUTHORIZED_EDIT", "You can only edit your own messages", HttpStatus.FORBIDDEN);
        }

        ChatParticipant participant = participantRepository.findByConversationIdAndUserId(message.getConversation().getId(), userId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                        HttpStatus.FORBIDDEN));
        if (participant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this conversation", HttpStatus.BAD_REQUEST);
        }

        message.setBody(newBody.trim());
        message.setEditedAt(LocalDateTime.now());
        messageRepository.save(message);

        ChatMessageDto dto = ChatMessageDto.builder()
                        .id(message.getId())
                        .conversationId(message.getConversation().getId())
                        .senderUsername(message.getSender().getUsername())
                        .body(message.getBody())
                        .messageType(message.getMessageType())
                        .attachmentUrl(message.getAttachmentUrl())
                        .sentAt(message.getSentAt().toString())
                        .editedAt(message.getEditedAt() == null ? null : message.getEditedAt().toString())
                        .build();
        messagingTemplate.convertAndSend("/topic/chat/" + message.getConversation().getId() + "/edit", dto);

        return dto;
    }

    @Transactional
    public String deleteMessage(CustomUserDetails customUserDetails, Long conversationId, Long messageId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();

        ChatMessage message = messageRepository.findByIdAndConversationId(messageId, conversationId)
                .orElseThrow(() -> new KnewitException("MESSAGE_NOT_FOUND", "Message not found in this chat",
                                                        HttpStatus.NOT_FOUND));

        if (message.getDeletedAt() != null) {
            throw new KnewitException("MESSAGE_ALREADY_DELETED", "Message already deleted", HttpStatus.BAD_REQUEST);
        }

        if (!message.getSender().getId().equals(userId)) {
            throw new KnewitException("UNAUTHORIZED_DELETE", "You can only delete your own messages", HttpStatus.FORBIDDEN);
        }

        ChatParticipant participant = participantRepository.findByConversationIdAndUserId(message.getConversation().getId(), userId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                        HttpStatus.FORBIDDEN));

        if (participant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this conversation", HttpStatus.BAD_REQUEST);
        }

        message.setDeletedAt(LocalDateTime.now());

        messageRepository.save(message);

        DeleteMessageDto dto = DeleteMessageDto.builder()
                        .messageId(message.getId())
                        .conversationId(message.getConversation().getId())
                        .build();

        messagingTemplate.convertAndSend("/topic/chat/" + message.getConversation().getId() + "/delete", dto);
        return "Message deleted successfully";
    }

    @Transactional
    public String addParticipant(CustomUserDetails customUserDetails, Long conversationId, Long participantUserId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long currentUserId = customUserDetails.getUserId();

        ChatConversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new KnewitException("CHAT_NOT_FOUND", "Conversation not found",
                                HttpStatus.NOT_FOUND));

        if (conversation.getConversationType() != ConversationType.GROUP) {
            throw new KnewitException("INVALID_CONVERSATION", "Participants can only be added to groups",
                                HttpStatus.BAD_REQUEST);
        }

        ChatParticipant creatorParticipant = participantRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                        .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant",
                                HttpStatus.FORBIDDEN));

        if (creatorParticipant.getLeftAt() != null) {
            throw new KnewitException("LEFT_GROUP", "You have left this group", HttpStatus.BAD_REQUEST);
        }

        if (!conversation.getCreatedBy().getId().equals(currentUserId)) {
            throw new KnewitException("UNAUTHORIZED_ACTION", "Only group creator can add participants",
                                HttpStatus.FORBIDDEN);
        }

        if (participantRepository.findByConversationIdAndUserId(conversationId, participantUserId).isPresent()) {
            throw new KnewitException("USER_ALREADY_EXISTS", "User already belongs to this group",
                    HttpStatus.BAD_REQUEST);
        }

        User participantUser = userRepository.findById(participantUserId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        ChatParticipant participant = ChatParticipant.builder()
                        .conversation(conversation)
                        .user(participantUser)
                        .build();

        participantRepository.save(participant);

        LocalDateTime now = LocalDateTime.now();

        ChatMessage systemMessage = ChatMessage.builder().conversation(conversation)
                        .sender(conversation.getCreatedBy())
                        .body(participantUser.getUsername() + " joined the group")
                        .messageType(MessageType.SYSTEM)
                        .sentAt(now)
                        .build();

        messageRepository.save(systemMessage);

        conversation.setLastMessageAt(now);

        conversationRepository.save(conversation);

        ChatMessageDto dto = ChatMessageDto.builder()
                        .id(systemMessage.getId())
                        .conversationId(conversationId)
                        .senderUsername(conversation.getCreatedBy().getUsername())
                        .body(systemMessage.getBody())
                        .messageType(MessageType.SYSTEM)
                        .sentAt(now.toString())
                        .build();

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return "Participant Added Successfully";
    }

    @Transactional(readOnly = true)
    public List<ChatParticipantDto> getAllParticipants(CustomUserDetails customUserDetails, Long conversationId) {

        if (customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long currentUserId = customUserDetails.getUserId();

        participantRepository.findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new KnewitException("UNAUTHORIZED_CHAT_ACCESS", "You are not a participant of this conversation",
                                                        HttpStatus.FORBIDDEN));

        return participantRepository
                .findAllByConversationId(conversationId)
                .stream()
                .filter(participant -> participant.getLeftAt() == null)
                .map(participant -> ChatParticipantDto.builder()
                                .userId(participant.getUser().getId())
                                .username(participant.getUser().getUsername())
                                .avatarUrl(participant.getUser().getAvatarUrl())
                                .avatarPublicId(participant.getUser().getAvatarPublicId())
                                .isMuted(participant.getIsMuted())
                                .build())
                .toList();
    }

    @Transactional
    public String leaveGroup(CustomUserDetails customUserDetails, Long conversationId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }
        Long userId = customUserDetails.getUserId();
        ChatConversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new KnewitException("CHAT_NOT_FOUND", "Conversation not found",
                                        HttpStatus.NOT_FOUND));
        if (conversation.getConversationType() != ConversationType.GROUP) {
            throw new KnewitException("INVALID_CONVERSATION", "Cannot leave a direct conversation", HttpStatus.BAD_REQUEST);
        }

        ChatParticipant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
                        .orElseThrow(() -> new KnewitException("NOT_A_PARTICIPANT", "User is not a participant",
                                        HttpStatus.NOT_FOUND));

        if (participant.getLeftAt() != null) {
            throw new KnewitException("ALREADY_LEFT", "You already left this group", HttpStatus.BAD_REQUEST);
        }

        participant.setLeftAt(LocalDateTime.now());

        participantRepository.save(participant);

        User user = participant.getUser();

        LocalDateTime now = LocalDateTime.now();

        ChatMessage systemMessage = ChatMessage.builder()
                        .conversation(conversation)
                        .sender(user)
                        .body(user.getUsername() + " left the group")
                        .messageType(MessageType.SYSTEM)
                        .sentAt(now)
                        .build();

        messageRepository.save(systemMessage);

        conversation.setLastMessageAt(now);

        conversationRepository.save(conversation);

        ChatMessageDto dto = ChatMessageDto.builder()
                        .id(systemMessage.getId())
                        .conversationId(conversationId)
                        .senderUsername(user.getUsername())
                        .body(systemMessage.getBody())
                        .messageType(MessageType.SYSTEM)
                        .sentAt(now.toString())
                        .build();

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return "You have left the group successfully";
    }

}
