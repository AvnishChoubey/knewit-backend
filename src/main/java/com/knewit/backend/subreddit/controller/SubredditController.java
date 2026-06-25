package com.knewit.backend.subreddit.controller;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.subreddit.dto.*;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.service.SubredditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<SubredditDto> getSubreddit(@PathVariable String name) {
        return ResponseEntity.ok(
                subredditService.getSubreddit(name)
        );
    }

    @PostMapping("/{subredditId}/ban")
    public ResponseEntity<Void> banMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long subredditId,
            @RequestBody BanMemberRequest request
    ) {
        subredditService.banMember(
                customUserDetails,
                subredditId,
                request
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subredditId}/icon")
    public ResponseEntity<Void> uploadIcon(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestPart(name = "icon") MultipartFile file
    ) {

        subredditService.uploadIcon(
                subredditId,
                customUserDetails,
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
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok(
                subredditService.getBannedMembers(
                        subredditId,
                        customUserDetails
                )
        );
    }

    @PatchMapping("/{subredditId}/banner")
    public ResponseEntity<Void> uploadBanner(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestPart(name = "banner") MultipartFile file
    ) {

        subredditService.uploadBanner(
                subredditId,
                customUserDetails,
                file
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{subredditId}/unban")
    public ResponseEntity<Void> unbanMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long subredditId,
            @RequestBody UnbanMemberRequest request
    ) {

        subredditService.unbanMember(
                customUserDetails,
                subredditId,
                request
        );

        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{subredditId}")
    public ResponseEntity<SubredditDto> updateSubreddit(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdateSubredditRequest request
    ) {

        return ResponseEntity.ok(
                subredditService.updateSubreddit(
                        subredditId,
                        customUserDetails,
                        request
                )
        );
    }

    @GetMapping("/{subredditId}/members")
    public ResponseEntity<List<SubredditMemberDto>> getMembers(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok(
                subredditService.getMembers(
                        subredditId,
                        customUserDetails
                )
        );
    }

    @PostMapping("/{subredditId}/join")
    public ResponseEntity<JoinSubredditResponse> joinSubreddit(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(
                subredditService.join(customUserDetails, subredditId )
        );
    }

    @PatchMapping("/{name}/approve-member")
    public ResponseEntity<Void> approveMembership(
            @PathVariable String name,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        subredditService.approveMembership(
                customUserDetails,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{name}/reject-member")
    public ResponseEntity<Void> rejectMembership(
            @PathVariable String name,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        subredditService.rejectMembership(
                customUserDetails,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{name}/moderators")
    public ResponseEntity<Void> addModerator(
            @PathVariable String name,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam Long userId) {

        subredditService.addModerator(
                customUserDetails,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{subredditId}/private")
    public ResponseEntity<SubredditDto> makePrivate(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok(
                subredditService.makePrivate(
                        subredditId,
                        customUserDetails
                )
        );
    }

    @PatchMapping("/{subredditId}/public")
    public ResponseEntity<SubredditDto> makePublic(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok(
                subredditService.makePublic(
                        subredditId,
                        customUserDetails
                )
        );
    }

    @DeleteMapping("/{name}/moderators")
    public ResponseEntity<Void> removeModerator(
            @PathVariable String name,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        subredditService.removeModerator(
                customUserDetails,
                name,
                userId
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{subredditId}/join-requests")
    public ResponseEntity<List<JoinRequestDto>> getPendingJoinRequests(
            @PathVariable Long subredditId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok(
                subredditService.getPendingJoinRequests(
                        subredditId,
                        customUserDetails
                )
        );
    }

    @PostMapping("/create")
    public ResponseEntity<SubredditDto> createSubreddit(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateSubredditRequest request) {

        return ResponseEntity.ok(
                subredditService.createSubreddit(customUserDetails, request)
        );
    }

    @GetMapping("/")
    public ResponseEntity<List<SubredditDto>> getSubredditsByTopic(@RequestParam(value = "topic", required = false) String topic) {

        return ResponseEntity.ok(
                subredditService.getSubredditsByTopic(topic)
        );
    }

    @GetMapping("/{subredditId}/pending")
    public ResponseEntity<Page<PostDto>> getPendingPosts(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                         @PathVariable Long subredditId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(subredditService.getPendingPosts(subredditId, customUserDetails, page, size));
    }

    @PatchMapping("/{subredditId}/posts/{postId}/approve")
    public ResponseEntity<PostDto> approvePost(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @PathVariable("subredditId") Long subredditId,
                                               @PathVariable("postId") Long postId
    ) {

        return ResponseEntity.ok(subredditService.approvePost(customUserDetails, subredditId, postId));
    }

    @GetMapping("/{subredditName}/posts")
    public ResponseEntity<Page<PostDto>> getPostsBySubreddit(@PathVariable String subredditName,
                                                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(subredditService.getPostsBySubreddit(subredditName, customUserDetails, page, size));
    }

    @PatchMapping("/{subredditId}/posts/{postId}/reject")
    public ResponseEntity<PostDto> rejectPost(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                              @PathVariable("subredditId") Long subredditId,
                                              @PathVariable("postId") Long postId) {

        return ResponseEntity.ok(subredditService.rejectPost(postId, customUserDetails, subredditId));
    }
}