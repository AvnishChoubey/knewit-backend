package com.knewit.backend.subreddit.controller;

import com.knewit.backend.subreddit.dto.*;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.service.SubredditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/{subredditId}/ban")
    public ResponseEntity<Void> banMember(
            @PathVariable Long subredditId,
            @RequestBody BanMemberRequest request
    ) {

        subredditService.banMember(
                subredditId,
                request
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subredditId}/icon")
    public ResponseEntity<Void> uploadIcon(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId,
            @RequestParam MultipartFile file
    ) {

        subredditService.uploadIcon(
                subredditId,
                moderatorId,
                file
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping("/{subredditId}/moderators")
    public ResponseEntity<List<SubredditMemberDto>> getModerators(
            @PathVariable Long subredditId
    ) {

        return ResponseEntity.ok(
                subredditService.getModerators(
                        subredditId
                )
        );
    }


    @GetMapping("/{subredditId}/banned-members")
    public ResponseEntity<List<SubredditMemberDto>> getBannedMembers(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                subredditService.getBannedMembers(
                        subredditId,
                        moderatorId
                )
        );
    }

    @PostMapping("/{subredditId}/banner")
    public ResponseEntity<Void> uploadBanner(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId,
            @RequestParam MultipartFile file
    ) {

        subredditService.uploadBanner(
                subredditId,
                moderatorId,
                file
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subredditId}/unban")
    public ResponseEntity<Void> unbanMember(
            @PathVariable Long subredditId,
            @RequestBody UnbanMemberRequest request
    ) {

        subredditService.unbanMember(
                subredditId,
                request
        );

        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{subredditId}")
    public ResponseEntity<SubredditDto> updateSubreddit(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId,
            @RequestBody UpdateSubredditRequest request
    ) {

        return ResponseEntity.ok(
                subredditService.updateSubreddit(
                        subredditId,
                        moderatorId,
                        request
                )
        );
    }

    @GetMapping("/{subredditId}/members")
    public ResponseEntity<List<SubredditMemberDto>> getMembers(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                subredditService.getMembers(
                        subredditId,
                        moderatorId
                )
        );
    }

    @PostMapping("/{subredditId}/join")
    public ResponseEntity<JoinSubredditResponse> joinSubreddit(
            @PathVariable Long subredditId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                subredditService.join(userId, subredditId )
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

    @PatchMapping("/{subredditId}/private")
    public ResponseEntity<SubredditDto> makePrivate(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                subredditService.makePrivate(
                        subredditId,
                        moderatorId
                )
        );
    }

    @PatchMapping("/{subredditId}/public")
    public ResponseEntity<SubredditDto> makePublic(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                subredditService.makePublic(
                        subredditId,
                        moderatorId
                )
        );
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

    @GetMapping("/{subredditId}/join-requests")
    public ResponseEntity<List<JoinRequestDto>> getPendingJoinRequests(
            @PathVariable Long subredditId,
            @RequestParam Long moderatorId
    ) {

        return ResponseEntity.ok(
                subredditService.getPendingJoinRequests(
                        subredditId,
                        moderatorId
                )
        );
    }

    @DeleteMapping("/{subredditId}/leave")
    public ResponseEntity<Void> leaveSubreddit(
            @PathVariable Long subredditId,
            @RequestParam Long userId
    ) {

        subredditService.leaveSubreddit(
                subredditId,
                userId
        );

        return ResponseEntity.noContent()
                .build();
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