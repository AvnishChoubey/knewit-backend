package com.knewit.backend.subreddit.controller;

import com.knewit.backend.subreddit.dto.CreateSubredditRequest;
import com.knewit.backend.subreddit.dto.JoinSubredditResponse;
import com.knewit.backend.subreddit.dto.SubredditDto;
import com.knewit.backend.subreddit.service.SubredditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subreddits")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;

    @GetMapping("/{name}")
    public ResponseEntity<SubredditDto> getSubreddit(
            @PathVariable String name) {

        return ResponseEntity.ok(
                subredditService.getSubreddit(name)
        );
    }

    @PostMapping("/{name}/join")
    public ResponseEntity<JoinSubredditResponse> joinSubreddit(
            @PathVariable String name,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                subredditService.join(userId, name)
        );
    }

    @PostMapping("/{name}/membership/{userId}/approve")
    public ResponseEntity<Void> approveMembership(
            @PathVariable String name,
            @PathVariable Long userId,
            @RequestParam Long moderatorId) {

        subredditService.approveMembership(
                moderatorId,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{name}/membership/{userId}/reject")
    public ResponseEntity<Void> rejectMembership(
            @PathVariable String name,
            @PathVariable Long userId,
            @RequestParam Long moderatorId) {

        subredditService.rejectMembership(
                moderatorId,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{name}/moderators/{userId}")
    public ResponseEntity<Void> addModerator(
            @PathVariable String name,
            @PathVariable Long userId,
            @RequestParam Long creatorId) {

        subredditService.addModerator(
                creatorId,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{name}/moderators/{userId}")
    public ResponseEntity<Void> removeModerator(
            @PathVariable String name,
            @PathVariable Long userId,
            @RequestParam Long creatorId) {

        subredditService.removeModerator(
                creatorId,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<SubredditDto> createSubreddit(
            @RequestParam Long creatorId,
            @Valid @RequestBody CreateSubredditRequest request) {

        return ResponseEntity.ok(
                subredditService.createSubreddit(creatorId, request)
        );
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<List<SubredditDto>> getSubredditsByTopic(
            @PathVariable String topic) {

        return ResponseEntity.ok(
                subredditService.getSubredditsByTopic(topic)
        );
    }

    @GetMapping
    public ResponseEntity<List<SubredditDto>> getAllSubreddits() {

        return ResponseEntity.ok(
                subredditService.getAllSubreddits()
        );
    }
}