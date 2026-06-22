package com.knewit.backend.user.controller;

import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.entity.UserFollow;
import com.knewit.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired private UserService userService;

    /* PROFILE APIS */

//    @GetMapping("/me")
//    public ResponseEntity<AuthenticatedUserDto> getMe(@RequestHeader("Authorization") String authHeader) {
//        Long userId = getUserIdFromToken(authHeader);
//        return ResponseEntity.ok(userService.getMe(userId));
//    }

//    @GetMapping("/")
//    public ResponseEntity<GetPublicUserResponse> getPublicUser(@RequestParam("username") String username) {
//        Long viewerId = (authHeader != null && !authHeader.isBlank()) ? getUserIdFromToken(authHeader) : 0L; // Dummy viewer if anonymous
//        return ResponseEntity.ok(userService.getPublicUser(viewerId, username));
//    }

    /*  SAVE APIS  */

    @GetMapping("/user/{userId}/save")
    public ResponseEntity<Map<?,?>> getAllSaves(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllSaves(userId));
    }

    @PostMapping("/user/{userId}/save")
    public ResponseEntity<Void> save(@PathVariable("userId") Long userId,
                                     @RequestParam(name = "entity") String entity,
                                     @RequestParam("entityId") Long entityId) {
        userService.save(userId, entity, entityId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}/save")
    public ResponseEntity<Void> unsave(@PathVariable("userId") Long userId,
                                       @RequestParam(name = "entity") String entity,
                                       @RequestParam("entityId") Long entityId) {
        userService.unsave(userId, entity, entityId);
        return ResponseEntity.ok().build();
    }

    /*  FOLLOW APIS  */

    @GetMapping("/user/{userId}/follow")
    public ResponseEntity<Map<?,?>> getAllFollows(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllFollows(userId));
    }

    @PostMapping("/user/{userId}/follow")
    public ResponseEntity<String> follow(@PathVariable("userId") Long userId,
                                         @RequestParam("entity") String entity,
                                         @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(userService.follow(userId, entity, entityId));
    }

    @DeleteMapping("/user/{userId}/follow")
    public ResponseEntity<String> unfollow(@PathVariable("userId") Long userId,
                                         @RequestParam("entity") String entity,
                                         @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(userService.unfollow(userId, entity, entityId));
    }

    /*  BLOCK APIS  */

    @GetMapping("/user/{userId}/block")
    public ResponseEntity<Map<?,?>> getAllBlocks(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getAllBlocks(userId));
    }

    @PostMapping("/user/{userId}/block")
    public ResponseEntity<String> block(@PathVariable("userId") Long userId,
                                        @RequestParam("entity") String entity,
                                        @RequestParam("entityId") Long entityId) {

        return ResponseEntity.ok(userService.block(userId, entity, entityId));
    }

    @DeleteMapping("/user/{userId}/block")
    public ResponseEntity<Void> unblock(@PathVariable("userId") Long userId,
                                        @RequestParam("entity") String entity,
                                        @RequestParam("entityId") Long entityId) {
        userService.unblock(userId, entity, entityId);
        return ResponseEntity.ok().build();
    }
}
