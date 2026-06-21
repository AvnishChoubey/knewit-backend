package com.knewit.backend.user.service;

//import com.knewit.backend.auth.dto.AuthenticatedUserDto;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.entity.*;
import com.knewit.backend.user.enums.ReportStatus;
import com.knewit.backend.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private UserFollowRepository userFollowRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private UserReportRepository userReportRepository;

//    @Transactional(readOnly = true)
//    public AuthenticatedUserDto getMe(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
//
//        return new AuthenticatedUserDto(
//                user.getId(),
//                user.getEmail(),
//                user.getUsername(),
//                List.of("USER"),
//                user.getUsername() != null,
//                user.getEmailVerifiedAt() != null
//        );
//    }

    @Transactional
    public UserProfileDto updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        
        if(request.getBio() != null) user.setBio(request.getBio());
        if(request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if(request.getAvatarPublicId() != null) user.setAvatarPublicId(request.getAvatarPublicId());
        
        userRepository.save(user);

        if(request.getInterests() != null) {
            userInterestRepository.deleteAllByUserId(userId);
            
            for(Topic interest : request.getInterests()) {
                UserInterest userInterest = UserInterest.builder()
                        .user(user)
                        .interest(interest)
                        .build();
                
                userInterestRepository.save(userInterest);
            }
        }

        return getUserProfileDto(user);
    }

    @Transactional(readOnly = true)
    public GetPublicUserResponse getPublicUser(Long viewerId, String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        boolean isFollowing = userFollowRepository.existsByFollowerIdAndFollowedId(viewerId, targetUser.getId());
        boolean isBlocked = userBlockRepository.existsByBlockerIdAndBlockedId(targetUser.getId(), viewerId);
        boolean isBlockedByViewer = userBlockRepository.existsByBlockerIdAndBlockedId(viewerId, targetUser.getId());

        UserProfileDto profileDto = getUserProfileDto(targetUser);

        return new GetPublicUserResponse(profileDto, isFollowing, isBlocked, isBlockedByViewer);
    }

    @Transactional
    public FollowUserResponse follow(Long followerId, String followedUsername) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));
        User followed = userRepository.findByUsername(followedUsername)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Followed user not found", HttpStatus.NOT_FOUND));

        if (follower.getId().equals(followed.getId())) {
            throw new KnewitException("CANNOT_FOLLOW_SELF", "Users cannot follow themselves", HttpStatus.CONFLICT);
        }

        if (userFollowRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId())) {
            throw new KnewitException("ALREADY_FOLLOWING", "Already following this user", HttpStatus.CONFLICT);
        }

        UserFollow follow = UserFollow.builder()
                .follower(follower)
                .followed(followed)
                .build();
        userFollowRepository.save(follow);

        long followingCount = userFollowRepository.countByFollowerId(follower.getId());
        return new FollowUserResponse(true, followingCount);
    }

    @Transactional
    public void unfollow(Long followerId, String followedUsername) {
        User followed = userRepository.findByUsername(followedUsername)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Followed user not found", HttpStatus.NOT_FOUND));

        if (!userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followed.getId())) {
            throw new KnewitException("NOT_FOLLOWING", "You are not following this user", HttpStatus.CONFLICT);
        }

        userFollowRepository.deleteByFollowerIdAndFollowedId(followerId, followed.getId());
    }

    @Transactional
    public BlockUserResponse block(Long blockerId, String blockedUsername) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));
        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocked user not found", HttpStatus.NOT_FOUND));

        if (blocker.getId().equals(blocked.getId())) {
            throw new KnewitException("CANNOT_BLOCK_SELF", "Users cannot block themselves", HttpStatus.CONFLICT);
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())) {
            throw new KnewitException("ALREADY_BLOCKED", "User is already blocked", HttpStatus.CONFLICT);
        }

        // Auto unfollow if they block
        userFollowRepository.deleteByFollowerIdAndFollowedId(blocker.getId(), blocked.getId());
        userFollowRepository.deleteByFollowerIdAndFollowedId(blocked.getId(), blocker.getId());

        UserBlock block = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        userBlockRepository.save(block);

        return new BlockUserResponse(true);
    }

    @Transactional
    public void unblock(Long blockerId, String blockedUsername) {
        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocked user not found", HttpStatus.NOT_FOUND));

        if (!userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blocked.getId())) {
            throw new KnewitException("NOT_BLOCKED", "User is not blocked", HttpStatus.CONFLICT);
        }

        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blocked.getId());
    }

    @Transactional
    public ReportUserResponse report(Long reporterId, String reportedUsername, String reason, String details) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Reporter user not found", HttpStatus.NOT_FOUND));
        User reported = userRepository.findByUsername(reportedUsername)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Reported user not found", HttpStatus.NOT_FOUND));

        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .details(details)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        UserReport savedUserReport = userReportRepository.save(report);

        return new ReportUserResponse(true, savedUserReport.getId());
    }

    private UserProfileDto getUserProfileDto(User user) {
        List<Topic> interests = userInterestRepository.findAllByUserId(user.getId()).stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toList());

        long followers = userFollowRepository.countByFollowedId(user.getId());
        long following = userFollowRepository.countByFollowerId(user.getId());

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .avatarPublicId(user.getAvatarPublicId())
                .interests(interests)
                .followersCount(followers)
                .followingCount(following)
                .build();
    }
}
