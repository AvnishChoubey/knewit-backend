package com.knewit.backend.chat.controller;

import com.knewit.backend.chat.dto.ChatMessageDto;
import com.knewit.backend.chat.service.ChatService;
//import com.knewit.backend.config.JwtConfig;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable Long chatId) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.getMessages(userId, chatId));
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
            @RequestHeader("Authorization")
            String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(chatService.getUserConversations(userId));
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
