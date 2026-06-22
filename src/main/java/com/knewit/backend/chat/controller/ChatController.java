package com.knewit.backend.chat.controller;

import com.knewit.backend.chat.dto.*;
import com.knewit.backend.chat.service.ChatService;
//import com.knewit.backend.config.JwtConfig;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
//    private final JwtConfig jwtConfig;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId, @RequestParam(defaultValue = "0")
            int page, @RequestParam(defaultValue = "50") int size) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.getMessages(userId, chatId, page, size));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId,
            @RequestParam String body) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.sendMessage(userId, chatId, body));
    }

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getConversations(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationDto> createDirectConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateDirectConversationDto dto) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.createDirectConversation(userId, dto.getTargetUserId()));
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationDto> createGroupConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateGroupConversationDto dto) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.createGroupConversation(userId, dto));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId,
            @RequestBody MarkAsReadDto dto) {
        Long userId = getUserIdFromToken(authHeader);
        chatService.markAsRead(userId, chatId, dto.getLastMessageId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageDto> editMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long messageId,
            @RequestBody EditMessageDto dto) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.editMessage(userId, messageId, dto.getBody()));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long messageId) {
        Long userId = getUserIdFromToken(authHeader);
        chatService.deleteMessage(userId, messageId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<Void> addParticipant(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId,
            @RequestBody AddParticipantDto dto) {
        Long userId = getUserIdFromToken(authHeader);
        chatService.addParticipant(userId, chatId, dto.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId) {
        Long userId = getUserIdFromToken(authHeader);
        chatService.leaveGroup(userId, chatId);
        return ResponseEntity.ok().build();
    }

//    private Long getUserIdFromToken(String authHeader) {
//        String token = authHeader.replace("Bearer ", "");
//        Key key = Keys.hmacShaKeyFor(jwtConfig.getSigningSecret().getBytes(StandardCharsets.UTF_8));
//        String userIdStr = Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//        return Long.fromString(userIdStr);
//    }
}
