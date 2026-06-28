package com.knewit.backend.user.service;

import com.knewit.backend.auth.dto.AuthenticatedUserDto;
import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.entity.CommentBlock;
import com.knewit.backend.comment.entity.CommentFollow;
import com.knewit.backend.comment.entity.CommentSave;
import com.knewit.backend.comment.repository.CommentBlockRepository;
import com.knewit.backend.comment.repository.CommentFollowRepository;
import com.knewit.backend.comment.repository.CommentRepository;
import com.knewit.backend.comment.repository.CommentSaveRepository;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.entity.PostBlock;
import com.knewit.backend.post.entity.PostFollow;
import com.knewit.backend.post.entity.PostSave;
import com.knewit.backend.post.enums.PostStatus;
import com.knewit.backend.post.repository.PostBlockRepository;
import com.knewit.backend.post.repository.PostFollowRepository;
import com.knewit.backend.post.repository.PostRepository;
import com.knewit.backend.post.repository.PostSaveRepository;
import com.knewit.backend.search.entity.UserDocument;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.entity.SubredditMember;
import com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.entity.*;
import com.knewit.backend.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private SubredditRepository subredditRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private SubredditMemberRepository subredditMemberRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private UserFollowRepository userFollowRepository;
    @Autowired private PostFollowRepository postFollowRepository;
    @Autowired private CommentFollowRepository commentFollowRepository;
    @Autowired private PostSaveRepository postSaveRepository;
    @Autowired private CommentSaveRepository commentSaveRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private PostBlockRepository postBlockRepository;
    @Autowired private CommentBlockRepository commentBlockRepository;

    @Transactional
    public UserProfileDto updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if(request.getBio() != null) user.setBio(request.getBio());
        if(request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if(request.getAvatarPublicId() != null) user.setAvatarPublicId(request.getAvatarPublicId());

        userRepository.save(user);

        if(request.getInterests() != null) {
            userInterestRepository.deleteAllByUser_Id(userId);

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

    @Transactional
    public AuthenticatedUserDto getMe(CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(customUserDetails.getUsername()).orElseThrow(() -> new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED));

        return AuthenticatedUserDto.builder()
                .email(user.getEmail())
                .userId(user.getId())
                .role(user.getRole())
                .profileCompleted(user.getProfileCompletedAt() != null)
                .verified(user.getEmailVerifiedAt() != null)
                .username(user.getUsername())
                .build();
    }

    @Transactional(readOnly = true)
    public GetPublicUserResponse getPublicUser(Long viewerId, String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        boolean isFollowing = userFollowRepository.existsByFollower_IdAndFollowed_Id(viewerId, targetUser.getId());
        boolean isBlocked = userBlockRepository.existsByBlocker_IdAndBlocked_Id(targetUser.getId(), viewerId);
        boolean isBlockedByViewer = userBlockRepository.existsByBlocker_IdAndBlocked_Id(viewerId, targetUser.getId());

        UserProfileDto profileDto = getUserProfileDto(targetUser);

        return new GetPublicUserResponse(profileDto, isFollowing, isBlocked, isBlockedByViewer);
    }

    /* SAVE FEATURE METHODS */

    public Map<?, ?> getAllSaves(CustomUserDetails customUserDetails, Long saverId) {
        User saver = userRepository.findById(saverId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

        if(saver.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        List<PostSave> postsaves = postSaveRepository.findAllBySaver(saver);
        List<CommentSave> commentsaves = commentSaveRepository.findAllBySaver(saver);

        Map<String, List<?>> response = new HashMap<>();

        response.put("savedPosts", postsaves);
        response.put("savedComments", commentsaves);

        return response;
    }

    @Transactional
    public String save(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User saver = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Saver user not found", HttpStatus.NOT_FOUND));

        if(saver.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        if(entity.equalsIgnoreCase("POST")) {
            Post saved = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Saved post not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlocker_IdAndBlocked_Id(saver.getId(), saved.getAuthor().getId());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Saved owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlocker_IdAndBlocked_Id(saved.getAuthor().getId(), saver.getId());

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Saved owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            Optional<PostSave> optionalPostSave = postSaveRepository.findBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
            if(optionalPostSave.isPresent()) {
                throw new KnewitException("ALREADY_SAVED", "You have already saved", HttpStatus.BAD_REQUEST);
            } else {
                PostSave postSave = PostSave.builder()
                        .saver(saver)
                        .saved(saved)
                        .build();

                postSaveRepository.save(postSave);
            }
        }
        else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment saved = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Saved comment not found", HttpStatus.NOT_FOUND));

            User savedCommentOwner = userRepository.findById(saved.getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlocker_IdAndBlocked_Id(saver.getId(), savedCommentOwner.getId());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Comment owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlocker_IdAndBlocked_Id(savedCommentOwner.getId(), saver.getId());

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Comment owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            User savedCommentPostOwner = userRepository.findById(postRepository.findById(saved.getPost().getId()).get().getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock3 = userBlockRepository.findByBlocker_IdAndBlocked_Id(saver.getId(), savedCommentPostOwner.getId());

            if(optionalUserBlock3.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock4 = userBlockRepository.findByBlocker_IdAndBlocked_Id(savedCommentPostOwner.getId(), saver.getId());

            if(optionalUserBlock4.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Post owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            Optional<CommentSave> optionalCommentSave = commentSaveRepository.findBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
            if(optionalCommentSave.isPresent()) {
                throw new KnewitException("ALREADY_SAVED", "You have already saved", HttpStatus.BAD_REQUEST);
            } else {
                CommentSave commentSave = CommentSave.builder()
                        .saver(saver)
                        .saved(saved)
                        .build();

                commentSaveRepository.save(commentSave);
            }
        }

        return "Saved successfully.";
    }

    @Transactional
    public void unsave(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User saver = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Saver user not found", HttpStatus.NOT_FOUND));

        if(saver.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        if(entity.equalsIgnoreCase("POST")) {
            Post saved = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Saved post not found", HttpStatus.NOT_FOUND));

            postSaveRepository.deleteBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
        } else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment saved = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Saved comment not found", HttpStatus.NOT_FOUND));

            commentSaveRepository.deleteBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
        }
    }


    /* FOLLOW FEATURE METHODS */

    public Map<?,?> getAllFollows(CustomUserDetails customUserDetails, Long followerId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

        if(follower.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        List<UserFollow> userFollows = userFollowRepository.findAllByFollower_Id(follower.getId());
        List<PostFollow> postFollows = postFollowRepository.findAllByFollower_Id(follower.getId());
        List<CommentFollow> commentFollows = commentFollowRepository.findAllByFollower_Id(follower.getId());

        Map<String, List<?>> response = new HashMap<>();

        response.put("followedUsers", userFollows);
        response.put("followedPosts", postFollows);
        response.put("followedComments", commentFollows);

        return response;
    }

    @Transactional
    public String follow(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User follower = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

        if(follower.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        if(entity.equalsIgnoreCase("USER")) {
            User followed = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Followed user not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlocker_IdAndBlocked_Id(follower.getId(), followed.getId());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Followed has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlocker_IdAndBlocked_Id(followed.getId(), follower.getId());

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Followed has already blocked you", HttpStatus.BAD_REQUEST);
            }

            Optional<UserFollow> optionalUserFollow = userFollowRepository.findByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
            if(optionalUserFollow.isPresent()) {
                throw new KnewitException("ALREADY_FOLLOWING", "You are already following", HttpStatus.BAD_REQUEST);
            } else {
                UserFollow userFollow = UserFollow.builder()
                        .follower(follower)
                        .followed(followed)
                        .build();

                userFollowRepository.save(userFollow);
            }
        }
        else if(entity.equalsIgnoreCase("POST")) {
            Post followed = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Followed post not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlocker_IdAndBlocked_Id(follower.getId(), followed.getAuthor().getId());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlocker_IdAndBlocked_Id(followed.getAuthor().getId(), follower.getId());

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            Optional<PostFollow> optionalPostFollow = postFollowRepository.findByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());

            if(optionalPostFollow.isPresent()) {
                throw new KnewitException("ALREADY_FOLLOWING", "You are already following", HttpStatus.BAD_REQUEST);
            } else {
                PostFollow postFollow = PostFollow.builder()
                        .follower(follower)
                        .followed(followed)
                        .build();

                postFollowRepository.save(postFollow);
            }
        }
        else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment followed = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Followed comment not found", HttpStatus.NOT_FOUND));

            User followedCommentOwner = userRepository.findById(followed.getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlocker_IdAndBlocked_Id(follower.getId(), followedCommentOwner.getId());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Comment owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlocker_IdAndBlocked_Id(followedCommentOwner.getId(), follower.getId());

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Comment owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            User followedCommentPostOwner = userRepository.findById(postRepository.findById(followed.getPost().getId()).get().getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock3 = userBlockRepository.findByBlocker_IdAndBlocked_Id(follower.getId(), followedCommentPostOwner.getId());

            if(optionalUserBlock3.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock4 = userBlockRepository.findByBlocker_IdAndBlocked_Id(followedCommentPostOwner.getId(), follower.getId());

            if(optionalUserBlock4.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            Optional<CommentFollow> optionalCommentFollow = commentFollowRepository.findByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
            if(optionalCommentFollow.isPresent()) {
                throw new KnewitException("ALREADY_FOLLOWING", "You are already following", HttpStatus.BAD_REQUEST);
            } else {
                CommentFollow commentFollow = CommentFollow.builder()
                        .follower(follower)
                        .followed(followed)
                        .build();

                commentFollowRepository.save(commentFollow);
            }
        }

        return "Followed successfully.";
    }

    @Transactional
    public String unfollow(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User follower = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

        if(follower.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        if(entity.equalsIgnoreCase("USER")) {
            User followed = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Followed user not found", HttpStatus.NOT_FOUND));

            userFollowRepository.deleteByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        } else if(entity.equalsIgnoreCase("POST")) {
            Post followed = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Followed post not found", HttpStatus.NOT_FOUND));

            postFollowRepository.deleteByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        } else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment followed = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Followed comment not found", HttpStatus.NOT_FOUND));

            commentFollowRepository.deleteByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        }

        return "Unfollowed successfully";
    }


    /* BLOCK FEATURE METHODS */

    public Map<?,?> getAllBlocks(CustomUserDetails customUserDetails, Long blockerId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

        if(blocker.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        List<UserBlock> userBlocks = userBlockRepository.findAllByBlocker_Id(blocker.getId());
        List<PostBlock> postBlocks = postBlockRepository.findAllByBlocker(blocker);
        List<CommentBlock> commentBlocks = commentBlockRepository.findAllByBlocker(blocker);

        Map<String, List<?>> response = new HashMap<>();

        response.put("blockedUsers", userBlocks);
        response.put("blockedPosts", postBlocks);
        response.put("blockedComments", commentBlocks);

        return response;
    }

    @Transactional
    public String block(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User blocker = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

        if(blocker.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        unfollow(customUserDetails, userId, entity, entityId);
        unsave(customUserDetails, userId, entity, entityId);

        if(entity.equalsIgnoreCase("USER")) {
            User blocked = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocked user not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock = userBlockRepository.findByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId());

            if(optionalUserBlock.isPresent()) {
                throw new KnewitException("ALREADY_BLOCKED", "You have already blocked this user", HttpStatus.BAD_REQUEST);
            } else {
                UserBlock userBlock = UserBlock.builder()
                        .blocker(blocker)
                        .blocked(blocked)
                        .build();

                userBlockRepository.save(userBlock);
            }
        }
        else if(entity.equalsIgnoreCase("POST")) {
            Post blocked = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Blocked post not found", HttpStatus.NOT_FOUND));

            Optional<PostBlock> optionalPostBlock = postBlockRepository.findByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId());

            if(optionalPostBlock.isPresent()) {
                throw new KnewitException("ALREADY_BLOCKED", "You have already blocking", HttpStatus.BAD_REQUEST);
            } else {
                PostBlock postBlock = PostBlock.builder()
                        .blocker(blocker)
                        .blocked(blocked)
                        .build();

                postBlockRepository.save(postBlock);
            }
        }
        else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment blocked = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Blocked comment not found", HttpStatus.NOT_FOUND));

            Optional<CommentBlock> optionalCommentBlock = commentBlockRepository.findByBlockerAndBlocked(blocker, blocked);

            if(optionalCommentBlock.isPresent()) {
                throw new KnewitException("ALREADY_BLOCKED", "You have already blocked", HttpStatus.BAD_REQUEST);
            } else {
                CommentBlock commentBlock = CommentBlock.builder()
                        .blocker(blocker)
                        .blocked(blocked)
                        .build();

                commentBlockRepository.save(commentBlock);
            }
        }

        return "Blocked successfully.";
    }

    @Transactional
    public String unblock(CustomUserDetails customUserDetails, Long userId, String entity, Long entityId) {
        User blocker = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

        if(blocker.getId() != customUserDetails.getUserId()) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        if(entity.equalsIgnoreCase("USER")) {
            User blocked = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocked user not found", HttpStatus.NOT_FOUND));

            userBlockRepository.deleteByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId());
        } else if(entity.equalsIgnoreCase("POST")) {
            Post blocked = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Blocked post not found", HttpStatus.NOT_FOUND));

            postBlockRepository.deleteByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId());
        } else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment blocked = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Blocked comment not found", HttpStatus.NOT_FOUND));

            commentBlockRepository.deleteByBlocker_IdAndBlocked_Id(blocker.getId(), blocked.getId());
        }

        return "Unblocked successfully";
    }


    /* SUBREDDIT METHODS */
    @Transactional
    public void leaveSubreddit(Long subredditId, CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new RuntimeException("Subreddit not found"));

        SubredditMember membership = subredditMemberRepository.findBySubreddit_IdAndUser_Id(subredditId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this subreddit"));

        if (subreddit.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Subreddit creator cannot leave the subreddit");
        }

        if (Boolean.TRUE.equals(membership.getIsModerator())) {
            long moderatorCount = subredditMemberRepository.countBySubreddit_IdAndIsModeratorTrue(subredditId);

            if (moderatorCount <= 1) {
                throw new RuntimeException("Last moderator cannot leave the subreddit");
            }
        }

        subredditMemberRepository.delete(membership);

        subreddit.setMemberCount(Math.max(0, subreddit.getMemberCount() - 1));

        subredditRepository.save(subreddit);
    }


    /* POSTS METHODS */
    @Transactional(readOnly = true)
    public Page<PostDto> getUserPosts(Long authorId, CustomUserDetails customUserDetails, int page, int size) {
        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L;

        UserBlock userblock = userBlockRepository.findByBlocker_IdAndBlocked_Id(authorId, viewerId).orElseThrow(() -> new KnewitException("ALREADY_BLOCKED", "You are blocked", HttpStatus.BAD_REQUEST));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepository.findByAuthor_IdAndPostStatus(authorId, PostStatus.PUBLISHED, pageable)
                .map(post -> postToPostDto(post));
    }

    private UserProfileDto getUserProfileDto(User user) {
        List<Topic> interests = userInterestRepository.findAllByUser_Id(user.getId()).stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toList());

        long followers = userFollowRepository.countByFollowed_Id(user.getId());
        long following = userFollowRepository.countByFollower_Id(user.getId());

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

    public PostDto postToPostDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .body(post.getBody())
                .externalUrl(post.getExternalUrl())
                .postStatus(PostStatus.PUBLISHED.toString())
                .authorUsername(post.getAuthor().getUsername())
                .createdAt(post.getCreatedAt().toString())
                .downvoteCount(post.getDownvoteCount())
                .upvoteCount(post.getUpvoteCount())
                .commentCount(post.getCommentCount())
                .build();
    }
}