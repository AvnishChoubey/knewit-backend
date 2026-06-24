package com.knewit.backend.user.controller;

import com.knewit.backend.auth.dto.AuthenticatedUserDto;
import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired private UserService userService;

    /* PROFILE APIS */

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserDto> getMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(userService.getMe(customUserDetails));
    }

    @GetMapping("/")
    public ResponseEntity<GetPublicUserResponse> getPublicUser(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                               @RequestParam("username") String username) {
        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L; // Dummy viewer if anonymous
        return ResponseEntity.ok(userService.getPublicUser(viewerId, username));
    }

    /* SUBREDDIT APIS */

    @DeleteMapping("/{userId}/leave-subreddit")
    public ResponseEntity<Void> leaveSubreddit(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestParam("subredditId") Long subredditId) {
        userService.leaveSubreddit(subredditId, customUserDetails);
        return ResponseEntity.ok().build();
    }


    /* POST APIS */

    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<PostDto>> getUserPosts(@PathVariable Long userId,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUserPosts(userId, customUserDetails, page, size));
    }

    /*  SAVE APIS  */

    @GetMapping("/{userId}/save")
    public ResponseEntity<Map<?,?>> getAllSaves(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllSaves(customUserDetails, userId));
    }

    @PostMapping("/{userId}/save")
    public ResponseEntity<Void> save(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("entity") String entity,
                                     @RequestParam("entityId") Long entityId) {
        userService.save(customUserDetails, userId, entity, entityId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/save")
    public ResponseEntity<Void> unsave(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                       @PathVariable("userId") Long userId,
                                       @RequestParam(name = "entity") String entity,
                                       @RequestParam("entityId") Long entityId) {
        userService.unsave(customUserDetails, userId, entity, entityId);
        return ResponseEntity.ok().build();
    }

    /*  FOLLOW APIS  */

    @GetMapping("/{userId}/follow")
    public ResponseEntity<Map<?,?>> getAllFollows(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllFollows(customUserDetails, userId));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> follow(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                         @PathVariable("userId") Long userId,
                                         @RequestParam("entity") String entity,
                                         @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(userService.follow(customUserDetails, userId, entity, entityId));
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<String> unfollow(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @PathVariable("userId") Long userId,
                                           @RequestParam("entity") String entity,
                                           @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(userService.unfollow(customUserDetails, userId, entity, entityId));
    }

    /*  BLOCK APIS  */

    @GetMapping("/{userId}/block")
    public ResponseEntity<Map<?,?>> getAllBlocks(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                 @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllBlocks(customUserDetails, userId));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<String> block(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                        @PathVariable("userId") Long userId,
                                        @RequestParam("entity") String entity,
                                        @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(userService.block(customUserDetails, userId, entity, entityId));
    }

    @DeleteMapping("/{userId}/block")
    public ResponseEntity<Void> unblock(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                        @PathVariable("userId") Long userId,
                                        @RequestParam("entity") String entity,
                                        @RequestParam("entityId") Long entityId) {
        userService.unblock(customUserDetails, userId, entity, entityId);
        return ResponseEntity.ok().build();
    }
}
