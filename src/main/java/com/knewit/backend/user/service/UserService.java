package com.knewit.backend.user.service;

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
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.entity.PostBlock;
import com.knewit.backend.post.entity.PostFollow;
import com.knewit.backend.post.entity.PostSave;
import com.knewit.backend.post.repository.PostBlockRepository;
import com.knewit.backend.post.repository.PostFollowRepository;
import com.knewit.backend.post.repository.PostRepository;
import com.knewit.backend.post.repository.PostSaveRepository;
import com.knewit.backend.user.dto.*;
import com.knewit.backend.user.entity.*;
import com.knewit.backend.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private UserFollowRepository userFollowRepository;
    @Autowired private PostFollowRepository postFollowRepository;
    @Autowired private CommentFollowRepository commentFollowRepository;
    @Autowired private PostSaveRepository postSaveRepository;
    @Autowired private CommentSaveRepository commentSaveRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private PostBlockRepository postBlockRepository;
    @Autowired private CommentBlockRepository commentBlockRepository;

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

    public Map<?, ?> getAllSaves(Long saverId) {
        User saver = userRepository.findById(saverId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));
        
        List<PostSave> postsaves = postSaveRepository.findAllBySaver(saver);
        List<CommentSave> commentsaves = commentSaveRepository.findAllBySaver(saver);

        Map<String, List<?>> response = new HashMap<>();

        response.put("savedPosts", postsaves);
        response.put("savedComments", commentsaves);

        return response;
    }

    @Transactional
    public String save(Long userId, String entity, Long entityId) {
        User saver = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Saver user not found", HttpStatus.NOT_FOUND));

        if(entity.equalsIgnoreCase("POST")) {
            Post saved = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Saved post not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlockerAndBlocked(saver, saved.getAuthor());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Saved owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlockerAndBlocked(saved.getAuthor(), saver);

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

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlockerAndBlocked(saver, savedCommentOwner);

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Comment owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlockerAndBlocked(savedCommentOwner, saver);

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Comment owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            User savedCommentPostOwner = userRepository.findById(postRepository.findById(saved.getPost().getId()).get().getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock3 = userBlockRepository.findByBlockerAndBlocked(saver, savedCommentPostOwner);

            if(optionalUserBlock3.isPresent()) {
                throw new KnewitException("CANNOT_SAVE", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock4 = userBlockRepository.findByBlockerAndBlocked(savedCommentPostOwner, saver);

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
    public void unsave(Long userId, String entity, Long entityId) {
        User saver = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Saver user not found", HttpStatus.NOT_FOUND));

        if(entity.equalsIgnoreCase("POST")) {
            Post saved = postRepository.findById(entityId).orElseThrow(() -> new KnewitException("POST_NOT_FOUND", "Saved post not found", HttpStatus.NOT_FOUND));

            postSaveRepository.deleteBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
        } else if(entity.equalsIgnoreCase("COMMENT")) {
            Comment saved = commentRepository.findById(entityId).orElseThrow(() -> new KnewitException("COMMET_NOT_FOUND", "Saved comment not found", HttpStatus.NOT_FOUND));

            commentSaveRepository.deleteBySaver_IdAndSaved_Id(saver.getId(), saved.getId());
        }
    }


    /* FOLLOW FEATURE METHODS */

    public Map<?,?> getAllFollows(Long followerId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

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
    public String follow(Long userId, String entity, Long entityId) {
        User follower = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

        if(entity.equalsIgnoreCase("USER")) {
            User followed = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Followed user not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlockerAndBlocked(follower, followed);

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Followed has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlockerAndBlocked(followed, follower);

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

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlockerAndBlocked(follower, followed.getAuthor());

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlockerAndBlocked(followed.getAuthor(), follower);

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

            Optional<UserBlock> optionalUserBlock1 = userBlockRepository.findByBlockerAndBlocked(follower, followedCommentOwner);

            if(optionalUserBlock1.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Comment owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock2 = userBlockRepository.findByBlockerAndBlocked(followedCommentOwner, follower);

            if(optionalUserBlock2.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Comment owner has already blocked you", HttpStatus.BAD_REQUEST);
            }

            User followedCommentPostOwner = userRepository.findById(postRepository.findById(followed.getPost().getId()).get().getAuthor().getId()).get();

            Optional<UserBlock> optionalUserBlock3 = userBlockRepository.findByBlockerAndBlocked(follower, followedCommentPostOwner);

            if(optionalUserBlock3.isPresent()) {
                throw new KnewitException("CANNOT_FOLLOW", "Post owner has already been blocked", HttpStatus.BAD_REQUEST);
            }

            Optional<UserBlock> optionalUserBlock4 = userBlockRepository.findByBlockerAndBlocked(followedCommentPostOwner, follower);

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
    public String unfollow(Long userId, String entity, Long entityId) {
        User follower = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Follower user not found", HttpStatus.NOT_FOUND));

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

    public Map<?,?> getAllBlocks(Long blockerId) {
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

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
    public String block(Long userId, String entity, Long entityId) {
        User blocker = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

        if(entity.equalsIgnoreCase("USER")) {
            User blocked = userRepository.findById(entityId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocked user not found", HttpStatus.NOT_FOUND));

            Optional<UserBlock> optionalUserBlock = userBlockRepository.findByBlockerAndBlocked(blocker, blocked);

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

            Optional<PostBlock> optionalPostBlock = postBlockRepository.findByBlockerAndBlocked(blocker, blocked);

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

        unfollow(userId, entity, entityId);
        unsave(userId, entity, entityId);

        return "Blocked successfully.";
    }

    @Transactional
    public String unblock(Long userId, String entity, Long entityId) {
        User blocker = userRepository.findById(userId).orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "Blocker user not found", HttpStatus.NOT_FOUND));

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


    /*-------------------------------------------------------------------*/

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
}
