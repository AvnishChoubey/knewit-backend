package com.knewit.backend.chat.controller;

import com.knewit.backend.chat.dto.*;
import com.knewit.backend.chat.service.ChatService;
import com.knewit.backend.auth.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(chatService.getMessages(customUserDetails.getUserId(), chatId, page, size));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long chatId,
            @Valid @RequestBody SendMessageDto dto) {

        ChatMessageDto response = chatService.sendMessage(customUserDetails.getUserId(), chatId, dto.getBody());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getConversations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(chatService.getUserConversations(customUserDetails.getUserId()));
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationDto> createDirectConversation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CreateDirectConversationDto dto) {

        ConversationDto response = chatService.createDirectConversation(customUserDetails.getUserId(), dto.getTargetUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationDto> createGroupConversation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody CreateGroupConversationDto dto) {

        ConversationDto response = chatService.createGroupConversation(customUserDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<String> markAsRead(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long chatId,
            @RequestBody MarkAsReadDto dto) {

        String response = chatService.markAsRead(customUserDetails.getUserId(), chatId, dto.getLastMessageId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageDto> editMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long messageId,
            @RequestBody EditMessageDto dto) {

        return ResponseEntity.ok(chatService.editMessage(customUserDetails.getUserId(), messageId, dto.getBody()));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long messageId) {

        String response = chatService.deleteMessage(customUserDetails.getUserId(), messageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<String> addParticipant(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long chatId,
            @RequestBody AddParticipantDto dto) {

        String response = chatService.addParticipant(customUserDetails.getUserId(), chatId, dto.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{chatId}/leave")
    public ResponseEntity<String> leaveGroup(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long chatId) {

        String response = chatService.leaveGroup(customUserDetails.getUserId(), chatId);
        return ResponseEntity.ok(response);
    }
}
