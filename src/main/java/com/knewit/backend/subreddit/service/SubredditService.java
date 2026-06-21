package com.knewit.backend.subreddit.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.dto.CreateSubredditRequest;
import com.knewit.backend.subreddit.dto.SubredditDto;
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
import com.knewit.backend.subreddit.dto.JoinSubredditResponse;
import com.knewit.backend.subreddit.entity.SubredditJoinRequest;
import com.knewit.backend.subreddit.enums.SubredditJoinRequestStatus;
import com.knewit.backend.subreddit.repository.SubredditJoinRequestRepository;

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

    @Transactional
    public JoinSubredditResponse join(Long userId,
                                      String subredditName) {

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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