package com.knewit.backend.user.controller;

//import com.knewit.backend.auth.dto.AuthenticatedUserDto;
//import com.knewit.backend.config.JwtConfig;
import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.service.UserService;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    @Autowired private UserService userService;
//    @Autowired private JwtConfig jwtConfig;

    @GetMapping("/user/me")
    public ResponseEntity<AuthenticatedUserDto> getMe(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PatchMapping("/profile/me")
    public ResponseEntity<UserProfileDto> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.updateMyProfile(userId, request));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<GetPublicUserResponse> getPublicUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String username) {
        Long viewerId = (authHeader != null && !authHeader.isBlank()) ? getUserIdFromToken(authHeader) : 0L; // Dummy viewer if anonymous
        return ResponseEntity.ok(userService.getPublicUser(viewerId, username));
    }

    @PostMapping("/users/{username}/follow")
    public ResponseEntity<FollowUserResponse> follow(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.follow(userId, username));
    }

    @DeleteMapping("/users/{username}/follow")
    public ResponseEntity<Void> unfollow(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username) {
        Long userId = getUserIdFromToken(authHeader);
        userService.unfollow(userId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{username}/block")
    public ResponseEntity<BlockUserResponse> block(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.block(userId, username));
    }

    @DeleteMapping("/users/{username}/block")
    public ResponseEntity<Void> unblock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username) {
        Long userId = getUserIdFromToken(authHeader);
        userService.unblock(userId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{username}/report")
    public ResponseEntity<ReportUserResponse> report(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String username,
            @RequestParam String reason,
            @RequestParam(required = false) String details) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.report(userId, username, reason, details));
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
