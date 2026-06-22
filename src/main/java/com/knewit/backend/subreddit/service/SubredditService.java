package com.knewit.backend.subreddit.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.dto.*;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.entity.SubredditMember;
import com.knewit.backend.subreddit.enums.MemberStatus;
import com.knewit.backend.subreddit.enums.PostingPolicy;
import com.knewit.backend.subreddit.enums.Visibility;
import com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.knewit.backend.subreddit.entity.SubredditJoinRequest;
import com.knewit.backend.subreddit.enums.SubredditJoinRequestStatus;
import com.knewit.backend.subreddit.repository.SubredditJoinRequestRepository;
import  com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import org.springframework.web.multipart.MultipartFile;
import com.knewit.backend.common.service.MediaService;
import com.knewit.backend.common.dto.MediaUploadResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubredditService {
    private final SubredditRepository subredditRepository;
    private final SubredditMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SubredditJoinRequestRepository joinRequestRepository;
    private final SubredditMemberRepository subredditMemberRepository;
    private final MediaService mediaService;


    public SubredditDto createSubreddit(Long creatorId, CreateSubredditRequest request) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (subredditRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subreddit already exists");
        }

        Subreddit subreddit = Subreddit.builder()
                .name(request.getName())
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .visibility(
                        request.getVisibility() == null
                                ? Visibility.PUBLIC
                                : Visibility.valueOf(request.getVisibility().toUpperCase())
                )
                .postingPolicy(
                        request.getPostingPolicy() == null
                                ? PostingPolicy.OPEN
                                : PostingPolicy.valueOf(request.getPostingPolicy().toUpperCase())
                )
                .topic(
                        request.getTopic() == null
                                ? Topic.OTHER
                                : Topic.valueOf(request.getTopic().toUpperCase())
                )
                .iconUrl(request.getIconUrl())
                .iconPublicId(request.getIconPublicId())
                .memberCount(1L)
                .postCount(0L)
                .isArchived(false)
                .build();

        subreddit = subredditRepository.save(subreddit);

        SubredditMember creatorMember = SubredditMember.builder()
                .subreddit(subreddit)
                .user(creator)
                .memberStatus(MemberStatus.APPROVED)
                .isModerator(true)
                .approvedBy(creator)
                .approvedAt(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .build();

        memberRepository.save(creatorMember);

        return convertToDto(subreddit);
    }

    @Transactional(readOnly = true)
    public List<SubredditMemberDto> getModerators(
            Long subredditId
    ) {

        return memberRepository
                .findBySubreddit_IdAndIsModeratorTrue(
                        subredditId
                )
                .stream()
                .map(this::convertMemberDto)
                .toList();
    }

    private SubredditMemberDto convertMemberDto(
            SubredditMember member
    ) {

        return SubredditMemberDto.builder()
                .userId(
                        member.getUser().getId()
                )
                .username(
                        member.getUser().getUsername()
                )
                .memberStatus(
                        member.getMemberStatus().name()
                )
                .isModerator(
                        member.getIsModerator()
                )
                .joinedAt(
                        member.getJoinedAt() != null
                                ? member.getJoinedAt().toString()
                                : null
                )
                .build();
    }

    @Transactional
    public SubredditDto makePublic(
            Long subredditId,
            Long moderatorId
    ) {

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                subredditId,
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can change subreddit visibility"
            );
        }

        subreddit.setVisibility(
                Visibility.PUBLIC
        );

        subredditRepository.save(subreddit);

        return convertToDto(subreddit);
    }

    @Transactional
    public void leaveSubreddit(
            Long subredditId,
            Long userId
    ) {

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subreddit not found"
                        ));

        SubredditMember membership =
                subredditMemberRepository
                        .findBySubreddit_IdAndUser_Id(
                                subredditId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User is not a member of this subreddit"
                                ));

        if (subreddit.getCreator()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "Subreddit creator cannot leave the subreddit"
            );
        }

        if (Boolean.TRUE.equals(
                membership.getIsModerator()
        )) {

            long moderatorCount =
                    subredditMemberRepository
                            .countBySubreddit_IdAndIsModeratorTrue(
                                    subredditId
                            );

            if (moderatorCount <= 1) {

                throw new RuntimeException(
                        "Last moderator cannot leave the subreddit"
                );
            }
        }

        subredditMemberRepository.delete(
                membership
        );

        subreddit.setMemberCount(
                Math.max(
                        0,
                        subreddit.getMemberCount() - 1
                )
        );

        subredditRepository.save(
                subreddit
        );
    }

    @Transactional(readOnly = true)
    public List<JoinRequestDto> getPendingJoinRequests(
            Long subredditId,
            Long moderatorId
    ) {

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                subredditId,
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can view pending requests"
            );
        }

        return subredditMemberRepository
                .findBySubreddit_IdAndMemberStatus(
                        subredditId,
                        MemberStatus.PENDING
                )
                .stream()
                .map(member ->
                        JoinRequestDto.builder()
                                .userId(
                                        member.getUser().getId()
                                )
                                .username(
                                        member.getUser().getUsername()
                                )
                                .memberStatus(
                                        member.getMemberStatus().name()
                                )
                                .requestedAt(
                                        member.getCreatedAt().toString()
                                )
                                .build()
                )
                .toList();
    }

    @Transactional
    public SubredditDto makePrivate(
            Long subredditId,
            Long moderatorId
    ) {

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                subredditId,
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can change subreddit visibility"
            );
        }

        subreddit.setVisibility(
                Visibility.PRIVATE
        );

        subredditRepository.save(subreddit);

        return convertToDto(subreddit);
    }



    @Transactional(readOnly = true)
    public SubredditDto getSubreddit(String subredditName) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        return convertToDto(subreddit);
    }

    @Transactional(readOnly = true)
    public List<SubredditDto> getAllSubreddits() {

        return subredditRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    private void validateModerator(
            Long subredditId,
            Long moderatorId
    ) {

        boolean isModerator =
                memberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                subredditId,
                                moderatorId
                        );

        if (!isModerator) {

            throw new RuntimeException(
                    "Only moderators can perform this action"
            );
        }
    }

    @Transactional
    public void banMember(
            Long subredditId,
            BanMemberRequest request
    ) {

        validateModerator(
                subredditId,
                request.getModeratorId()
        );

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subreddit not found"
                        ));

        SubredditMember member =
                memberRepository
                        .findBySubreddit_IdAndUser_Username(
                                subredditId,
                                request.getUsername()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Member not found"
                                ));

        if (member.getUser()
                .getId()
                .equals(
                        subreddit.getCreator().getId()
                )) {

            throw new RuntimeException(
                    "Cannot ban subreddit creator"
            );
        }

        if (Boolean.TRUE.equals(
                member.getIsModerator()
        )) {

            throw new RuntimeException(
                    "Cannot ban moderator"
            );
        }

        if (member.getMemberStatus()
                == MemberStatus.BANNED) {

            throw new RuntimeException(
                    "User already banned"
            );
        }

        User moderator = userRepository
                .findById(
                        request.getModeratorId()
                )
                .orElseThrow();

        member.setMemberStatus(
                MemberStatus.BANNED
        );

        member.setBannedBy(
                moderator
        );

        member.setBannedAt(
                LocalDateTime.now()
        );

        member.setBanReason(
                request.getReason()
        );

        memberRepository.save(member);

        subreddit.setMemberCount(
                Math.max(
                        0,
                        subreddit.getMemberCount() - 1
                )
        );

        subredditRepository.save(subreddit);
    }

    @Transactional(readOnly = true)
    public List<SubredditMemberDto> getMembers(
            Long subredditId,
            Long moderatorId
    ) {

        validateModerator(
                subredditId,
                moderatorId
        );

        return memberRepository
                .findBySubreddit_Id(
                        subredditId
                )
                .stream()
                .map(member ->
                        SubredditMemberDto.builder()
                                .userId(
                                        member.getUser().getId()
                                )
                                .username(
                                        member.getUser().getUsername()
                                )
                                .memberStatus(
                                        member.getMemberStatus().name()
                                )
                                .isModerator(
                                        member.getIsModerator()
                                )
                                .joinedAt(
                                        member.getJoinedAt() != null
                                                ? member.getJoinedAt().toString()
                                                : null
                                )
                                .build()
                )
                .toList();
    }





    @Transactional
    public void unbanMember(
            Long subredditId,
            UnbanMemberRequest request
    ) {

        validateModerator(
                subredditId,
                request.getModeratorId()
        );

        SubredditMember member =
                memberRepository
                        .findBySubreddit_IdAndUser_Username(
                                subredditId,
                                request.getUsername()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Member not found"
                                ));

        if (member.getMemberStatus()
                != MemberStatus.BANNED) {

            throw new RuntimeException(
                    "User is not banned"
            );
        }

        member.setMemberStatus(
                MemberStatus.APPROVED
        );

        member.setBannedAt(null);

        member.setBannedBy(null);

        member.setBanReason(null);

        memberRepository.save(member);

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow();

        subreddit.setMemberCount(
                subreddit.getMemberCount() + 1
        );

        subredditRepository.save(subreddit);
    }

    @Transactional
    public JoinSubredditResponse join(Long userId,
                                      Long subredditId) {



        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        if (memberRepository.findBySubreddit_IdAndUser_Id(
                subreddit.getId(),
                userId
        ).isPresent()) {

            throw new RuntimeException("User is already a member");
        }

        if (subreddit.getVisibility() == Visibility.PUBLIC) {

            SubredditMember member = SubredditMember.builder()
                    .subreddit(subreddit)
                    .user(user)
                    .memberStatus(MemberStatus.APPROVED)
                    .isModerator(false)
                    .approvedAt(LocalDateTime.now())
                    .joinedAt(LocalDateTime.now())
                    .build();

            memberRepository.save(member);

            subreddit.setMemberCount(
                    subreddit.getMemberCount() + 1
            );

            subredditRepository.save(subreddit);

            return new JoinSubredditResponse(
                    "APPROVED",
                    "Successfully joined subreddit"
            );
        }

        SubredditJoinRequest joinRequest = SubredditJoinRequest.builder()
                .subreddit(subreddit)
                .requester(user)
                .status(SubredditJoinRequestStatus.PENDING)
                .build();

        joinRequestRepository.save(joinRequest);

        SubredditMember pendingMember = SubredditMember.builder()
                .subreddit(subreddit)
                .user(user)
                .memberStatus(MemberStatus.PENDING)
                .isModerator(false)
                .build();

        memberRepository.save(pendingMember);

        return new JoinSubredditResponse(
                "PENDING",
                "Join request submitted successfully"
        );
    }

    @Transactional
    public void approveMembership(Long moderatorId,
                                  String subredditName,
                                  Long targetUserId) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        SubredditMember moderator = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        moderatorId
                )
                .orElseThrow(() -> new RuntimeException("Moderator not found"));

        if (!moderator.getIsModerator()) {
            throw new RuntimeException("Only moderators can approve requests");
        }

        SubredditMember member = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        targetUserId
                )
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        member.setMemberStatus(MemberStatus.APPROVED);
        member.setApprovedBy(moderator.getUser());
        member.setApprovedAt(LocalDateTime.now());
        member.setJoinedAt(LocalDateTime.now());

        memberRepository.save(member);

        subreddit.setMemberCount(
                subreddit.getMemberCount() + 1
        );

        subredditRepository.save(subreddit);
    }

    @Transactional
    public void rejectMembership(Long moderatorId,
                                 String subredditName,
                                 Long targetUserId) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        SubredditMember moderator = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        moderatorId
                )
                .orElseThrow(() -> new RuntimeException("Moderator not found"));

        if (!moderator.getIsModerator()) {
            throw new RuntimeException("Only moderators can reject requests");
        }

        SubredditMember member = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        targetUserId
                )
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        memberRepository.delete(member);
    }

    @Transactional
    public void addModerator(Long creatorId,
                             String subredditName,
                             Long targetUserId) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        if (!subreddit.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Only creator can add moderators");
        }

        SubredditMember member = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        targetUserId
                )
                .orElseThrow(() -> new RuntimeException("User is not a member"));

        if (member.getMemberStatus() != MemberStatus.APPROVED) {
            throw new RuntimeException("Only approved members can become moderators");
        }

        member.setIsModerator(true);

        memberRepository.save(member);
    }


    @Transactional
    public SubredditDto updateSubreddit(
            Long subredditId,
            Long moderatorId,
            UpdateSubredditRequest request
    ) {

        validateModerator(
                subredditId,
                moderatorId
        );

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subreddit not found"
                        ));

        if (request.getTitle() != null) {
            subreddit.setTitle(
                    request.getTitle()
            );
        }

        if (request.getDescription() != null) {
            subreddit.setDescription(
                    request.getDescription()
            );
        }

        if (request.getTopic() != null) {
            subreddit.setTopic(
                    request.getTopic()
            );
        }

        if (request.getPostingPolicy() != null) {
            subreddit.setPostingPolicy(
                    request.getPostingPolicy()
            );
        }

        subredditRepository.save(
                subreddit
        );

        return convertToDto(
                subreddit
        );
    }

    @Transactional
    public void removeModerator(Long creatorId,
                                String subredditName,
                                Long targetUserId) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        if (!subreddit.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Only creator can remove moderators");
        }

        SubredditMember member = memberRepository
                .findBySubreddit_IdAndUser_Id(
                        subreddit.getId(),
                        targetUserId
                )
                .orElseThrow(() -> new RuntimeException("Moderator not found"));

        member.setIsModerator(false);

        memberRepository.save(member);
    }

    @Transactional
    public void uploadIcon(
            Long subredditId,
            Long moderatorId,
            MultipartFile file
    ) {

        validateModerator(
                subredditId,
                moderatorId
        );

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subreddit not found"
                        ));

        MediaUploadResponse response =
                mediaService.uploadFile(
                        file,
                        "knewit/subreddits/icons"
                );

        subreddit.setIconUrl(
                response.getUrl()
        );

        subreddit.setIconPublicId(
                response.getPublicId()
        );

        subredditRepository.save(
                subreddit
        );
    }


    @Transactional
    public void uploadBanner(
            Long subredditId,
            Long moderatorId,
            MultipartFile file
    ) {

        validateModerator(
                subredditId,
                moderatorId
        );

        Subreddit subreddit = subredditRepository
                .findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subreddit not found"
                        ));

        MediaUploadResponse response =
                mediaService.uploadFile(
                        file,
                        "knewit/subreddits/banners"
                );

        subreddit.setBannerUrl(
                response.getUrl()
        );

        subreddit.setBannerPublicId(
                response.getPublicId()
        );

        subredditRepository.save(
                subreddit
        );
    }


    @Transactional(readOnly = true)
    public List<SubredditMemberDto> getBannedMembers(
            Long subredditId,
            Long moderatorId
    ) {

        validateModerator(
                subredditId,
                moderatorId
        );

        return memberRepository
                .findBySubreddit_IdAndMemberStatus(
                        subredditId,
                        MemberStatus.BANNED
                )
                .stream()
                .map(this::convertMemberDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubredditDto> getSubredditsByTopic(String topic) {

        return subredditRepository
                .findByTopic(Topic.valueOf(topic.toUpperCase()))
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    private SubredditDto convertToDto(Subreddit subreddit) {

        return SubredditDto.builder()
                .id(subreddit.getId())
                .name(subreddit.getName())
                .title(subreddit.getTitle())
                .description(subreddit.getDescription())
                .topic(subreddit.getTopic().name())
                .visibility(subreddit.getVisibility().name())
                .postingPolicy(subreddit.getPostingPolicy().name())
                .iconUrl(subreddit.getIconUrl())
                .iconPublicId(subreddit.getIconPublicId())
                .creatorUsername(subreddit.getCreator().getUsername())
                .memberCount(subreddit.getMemberCount())
                .postCount(subreddit.getPostCount())
                .isArchived(subreddit.getIsArchived())
                .createdAt(subreddit.getCreatedAt().toString())
                .build();
    }
}