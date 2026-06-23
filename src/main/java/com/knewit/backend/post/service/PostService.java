package com.knewit.backend.post.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.enums.VoteType;
import com.knewit.backend.common.service.MediaService;
import com.knewit.backend.post.dto.CreatePostRequest;
import com.knewit.backend.post.dto.PostDto;
import com.knewit.backend.post.dto.UpdatePostRequest;
import com.knewit.backend.post.entity.*;
import com.knewit.backend.post.enums.FeedSort;
import com.knewit.backend.post.enums.MediaType;
import com.knewit.backend.post.enums.PostStatus;
import com.knewit.backend.post.enums.PostType;
import com.knewit.backend.post.repository.*;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.enums.MemberStatus;
import com.knewit.backend.subreddit.enums.PostingPolicy;
import com.knewit.backend.subreddit.enums.Visibility;
import com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.knewit.backend.post.dto.VotePostRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostVoteRepository postVoteRepository;
    private final PostSaveRepository postSaveRepository;
    private final MediaService mediaService;
    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final SubredditMemberRepository subredditMemberRepository;
    private final PostFollowRepository postFollowRepository;

    @Transactional
    public PostDto createPost(
            Long authorId,
            CreatePostRequest request
    ) {

        Subreddit subreddit = subredditRepository
                .findByName(request.getSubredditName())
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        User author = userRepository
                .findById(authorId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        PostType postType =
                PostType.valueOf(
                        request.getType().toUpperCase()
                );

        if (subreddit.getVisibility() == Visibility.PRIVATE) {

            boolean isApprovedMember =
                    subredditMemberRepository
                            .existsBySubreddit_IdAndUser_IdAndMemberStatus(
                                    subreddit.getId(),
                                    authorId,
                                    MemberStatus.APPROVED
                            );

            if (!isApprovedMember) {
                throw new RuntimeException(
                        "Only approved members can post in private subreddits"
                );
            }
        }

        if ((postType == PostType.IMAGE
                || postType == PostType.VIDEO)
                && request.getMedia() == null) {

            throw new RuntimeException(
                    "Media file is required"
            );
        }

        if (postType == PostType.TEXT
                && (request.getBody() == null
                || request.getBody().isBlank())) {

            throw new RuntimeException(
                    "Body is required for text posts"
            );
        }

        if (postType == PostType.URL
                && (request.getExternalUrl() == null
                || request.getExternalUrl().isBlank())) {

            throw new RuntimeException(
                    "External URL is required"
            );
        }

        PostStatus postStatus =
                subreddit.getPostingPolicy() == PostingPolicy.RESTRICTED
                        ? PostStatus.PENDING_APPROVAL
                        : PostStatus.PUBLISHED;

        Post post = Post.builder()
                .subreddit(subreddit)
                .author(author)
                .type(postType)
                .title(request.getTitle())
                .body(request.getBody())
                .externalUrl(request.getExternalUrl())
                .postStatus(postStatus)
                .upvoteCount(1L)
                .downvoteCount(0L)
                .shareCount(0L)
                .commentCount(0L)
                .reportCount(0L)
                .scoreHot(1.0)
                .scoreBest(1.0)
                .scoreRising(1.0)
                .scoreTop(1.0)
                .build();

        post = postRepository.save(post);

        PostVote selfVote = PostVote.builder()
                .post(post)
                .user(author)
                .voteType(VoteType.UPVOTE)
                .build();

        postVoteRepository.save(selfVote);

        if (postType == PostType.IMAGE
                || postType == PostType.VIDEO) {

            MediaUploadResponse mediaUploadResponse =
                    mediaService.uploadFile(
                            request.getMedia(),
                            "knewit/posts"
                    );

            PostMedia media = PostMedia.builder()
                    .post(post)
                    .mediaType(
                            postType == PostType.VIDEO
                                    ? MediaType.VIDEO
                                    : MediaType.IMAGE
                    )
                    .cloudinaryUrl(
                            mediaUploadResponse.getUrl()
                    )
                    .cloudinaryPublicId(
                            mediaUploadResponse.getPublicId()
                    )
                    .byteSize(
                            request.getMedia().getSize()
                    )
                    .build();

            postMediaRepository.save(media);
        }

        if (postStatus == PostStatus.PUBLISHED) {

            subreddit.setPostCount(
                    subreddit.getPostCount() + 1
            );

            subredditRepository.save(subreddit);
        }

        return convertToDto(
                post,
                author.getId()
        );
    }

    @Transactional
    public PostDto rejectPost(
            Long postId,
            Long moderatorId
    ) {

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Post not found"
                        ));

        if (post.getPostStatus() != PostStatus.PENDING_APPROVAL) {
            throw new RuntimeException(
                    "Post is not pending approval"
            );
        }

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                post.getSubreddit().getId(),
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can reject posts"
            );
        }

        post.setPostStatus(
                PostStatus.REMOVED
        );

        postRepository.save(post);

        return convertToDto(
                post,
                moderatorId
        );
    }


    @Transactional
    public PostDto approvePost(
            Long postId,
            Long moderatorId
    ) {

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Post not found"
                        ));

        if (post.getPostStatus() != PostStatus.PENDING_APPROVAL) {
            throw new RuntimeException(
                    "Post is not pending approval"
            );
        }

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                post.getSubreddit().getId(),
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can approve posts"
            );
        }

        post.setPostStatus(
                PostStatus.PUBLISHED
        );

        postRepository.save(post);

        Subreddit subreddit = post.getSubreddit();

        subreddit.setPostCount(
                subreddit.getPostCount() + 1
        );

        subredditRepository.save(subreddit);

        return convertToDto(
                post,
                moderatorId
        );
    }


    @Transactional(readOnly = true)
    public Page<PostDto> getPendingPosts(
            Long subredditId,
            Long moderatorId,
            int page,
            int size
    ) {

        boolean isModerator =
                subredditMemberRepository
                        .existsBySubreddit_IdAndUser_IdAndIsModeratorTrue(
                                subredditId,
                                moderatorId
                        );

        if (!isModerator) {
            throw new RuntimeException(
                    "Only moderators can view pending posts"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        return postRepository
                .findBySubreddit_IdAndPostStatus(
                        subredditId,
                        PostStatus.PENDING_APPROVAL,
                        pageable
                )
                .map(post -> convertToDto(
                        post,
                        moderatorId
                ));
    }



    @Transactional
    public PostDto updatePost(
            Long postId,
            Long authorId,
            UpdatePostRequest request
    ) {

        Post post = postRepository
                .findByIdAndAuthor_Id(postId, authorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Post not found or unauthorized"
                        ));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }

        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }

        if (request.getExternalUrl() != null) {
            post.setExternalUrl(request.getExternalUrl());
        }

        post = postRepository.save(post);

        return convertToDto(post, authorId);
    }


    @Transactional(readOnly = true)
    public Page<PostDto> getPostsBySubreddit(
            String subredditName,
            Long viewerId,
            int page,
            int size
    ) {

        Subreddit subreddit = subredditRepository
                .findByName(subredditName)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        if (subreddit.getVisibility() == Visibility.PRIVATE) {

            boolean isApprovedMember =
                    subredditMemberRepository
                            .existsBySubreddit_IdAndUser_IdAndMemberStatus(
                                    subreddit.getId(),
                                    viewerId,
                                    MemberStatus.APPROVED
                            );

            if (!isApprovedMember) {
                throw new RuntimeException(
                        "This is a private subreddit"
                );
            }
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return postRepository
                .findBySubreddit_NameAndPostStatus(
                        subredditName,
                        PostStatus.PUBLISHED,
                        pageable
                )
                .map(post -> convertToDto(post, viewerId));
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long postId, Long viewerId) {

        Post post = postRepository
                .findByIdAndPostStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        return convertToDto(post, viewerId);
    }


    @Transactional
    public void deletePost(
            Long postId,
            Long authorId
    ) {

        Post post = postRepository
                .findByIdAndAuthor_Id(postId, authorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Post not found or unauthorized"
                        ));

        post.setPostStatus(PostStatus.ARCHIVED);

        postRepository.save(post);
    }


    @Transactional
    public PostDto votePost(
            Long postId,
            Long userId,
            VotePostRequest request
    ) {

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        VoteType newVoteType =
                VoteType.valueOf(
                        request.getVoteType().toUpperCase()
                );

        PostVote existingVote = postVoteRepository
                .findByPost_IdAndUser_Id(
                        postId,
                        userId
                )
                .orElse(null);

        if (existingVote == null) {

            PostVote vote = PostVote.builder()
                    .post(post)
                    .user(user)
                    .voteType(newVoteType)
                    .build();

            postVoteRepository.save(vote);

            if (newVoteType == VoteType.UPVOTE) {
                post.setUpvoteCount(
                        post.getUpvoteCount() + 1
                );
            } else {
                post.setDownvoteCount(
                        post.getDownvoteCount() + 1
                );
            }

        } else {

            if (existingVote.getVoteType() == newVoteType) {

                if (newVoteType == VoteType.UPVOTE) {
                    post.setUpvoteCount(
                            post.getUpvoteCount() - 1
                    );
                } else {
                    post.setDownvoteCount(
                            post.getDownvoteCount() - 1
                    );
                }

                postVoteRepository.delete(existingVote);

            } else {

                if (existingVote.getVoteType() == VoteType.UPVOTE) {

                    post.setUpvoteCount(
                            post.getUpvoteCount() - 1
                    );

                    post.setDownvoteCount(
                            post.getDownvoteCount() + 1
                    );

                } else {

                    post.setDownvoteCount(
                            post.getDownvoteCount() - 1
                    );

                    post.setUpvoteCount(
                            post.getUpvoteCount() + 1
                    );
                }

                existingVote.setVoteType(newVoteType);

                postVoteRepository.save(existingVote);
            }
        }

        updateScores(post);

        postRepository.save(post);

        return convertToDto(post, userId);
    }



    @Transactional
    public boolean toggleSavePost(
            Long postId,
            Long userId
    ) {

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        PostSave existingSave = postSaveRepository
                .findBySaverIdAndSavedId(userId, postId)
                .orElse(null);

        if (existingSave != null) {

            postSaveRepository.delete(existingSave);

            return false;
        }

        PostSave save = PostSave.builder()
                .saved(post)
                .saver(user)
                .build();

        postSaveRepository.save(save);

        return true;
    }

    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(
            String keyword,
            Long viewerId,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return postRepository
                .searchPosts(
                        keyword,
                        PostStatus.PUBLISHED,
                        pageable
                )
                .map(post -> convertToDto(post, viewerId));
    }

    @Transactional(readOnly = true)
    public List<PostDto> getSavedPosts(Long userId) {

        return postSaveRepository
                .findBySaverId(userId)
                .stream()
                .map(save ->
                        convertToDto(
                                save.getSaved(),
                                userId
                        ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getUserPosts(
            Long authorId,
            Long viewerId,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return postRepository
                .findByAuthor_IdAndPostStatus(
                        authorId,
                        PostStatus.PUBLISHED,
                        pageable
                )
                .map(post ->
                        convertToDto(
                                post,
                                viewerId
                        ));
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getFeed(
            Long viewerId,
            int page,
            int size,
            String sort
    ) {

        FeedSort feedSort =
                FeedSort.valueOf(
                        sort.toUpperCase()
                );

        Pageable pageable;

        switch (feedSort) {

            case TOP -> pageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "scoreTop"
                            )
                    );

            case HOT -> pageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "scoreHot"
                            )
                    );

            case RISING -> pageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "scoreRising"
                            )
                    );

            default -> pageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "createdAt"
                            )
                    );
        }

        return postRepository
                .findByPostStatus(
                        PostStatus.PUBLISHED,
                        pageable
                )
                .map(post ->
                        convertToDto(
                                post,
                                viewerId
                        ));
    }

    @Transactional(readOnly = true)
    public List<PostDto> getFollowedPosts(
            Long userId
    ) {

        return postFollowRepository
                .findAllByFollowerId(userId)
                .stream()
                .map(PostFollow::getFollowed)
                .map(post ->
                        convertToDto(
                                post,
                                userId
                        )
                )
                .toList();
    }

    @Transactional
    public Boolean toggleFollowPost(
            Long postId,
            Long userId
    ) {

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Post not found"
                        ));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        Optional<PostFollow> existingFollow =
                postFollowRepository
                        .findByFollowerIdAndFollowedId(userId, postId);

        if (existingFollow.isPresent()) {

            postFollowRepository.delete(
                    existingFollow.get()
            );

            return false;
        }

        PostFollow follower =
                PostFollow.builder()
                        .followed(post)
                        .follower(user)
                        .build();

        postFollowRepository.save(follower);

        return true;
    }

    private PostDto convertToDto(
            Post post,
            Long viewerId
    ) {

        boolean followed = false;

        if (viewerId != null) {

            followed =
                    postFollowRepository
                            .existsByFollwerIdAndFollwedId(viewerId, post.getId());
        }

        boolean saved = false;

        if (viewerId != null) {
            saved = postSaveRepository
                    .existsBySaverIdAndSavedId(viewerId, post.getId());
        }

        String votedState = "NONE";

        PostVote vote = postVoteRepository
                .findByPost_IdAndUser_Id(
                        post.getId(),
                        viewerId
                )
                .orElse(null);

        if (vote != null) {
            votedState = vote.getVoteType().name();
        }

        PostMedia media = postMediaRepository
                .findAllByPost_Id(post.getId())
                .stream()
                .findFirst()
                .orElse(null);

        return PostDto.builder()
                .id(post.getId())
                .subredditName(post.getSubreddit().getName())
                .authorUsername(post.getAuthor().getUsername())
                .title(post.getTitle())
                .type(post.getType().name())
                .body(post.getBody())
                .externalUrl(post.getExternalUrl())
                .mediaUrl(
                        media != null
                                ? media.getCloudinaryUrl()
                                : null
                )
                .mediaPublicId(
                        media != null
                                ? media.getCloudinaryPublicId()
                                : null
                )
                .postStatus(post.getPostStatus().name())
                .upvoteCount(post.getUpvoteCount())
                .downvoteCount(post.getDownvoteCount())
                .shareCount(post.getShareCount())
                .commentCount(post.getCommentCount())
                .scoreHot(post.getScoreHot())
                .scoreBest(post.getScoreBest())
                .scoreRising(post.getScoreRising())
                .scoreTop(post.getScoreTop())
                .createdAt(post.getCreatedAt().toString())
                .votedState(votedState)
                .saved(saved)
                .followed(followed)
                .build();
    }

    private void updateScores(Post post) {

        long score =
                post.getUpvoteCount()
                        - post.getDownvoteCount();

        double calculatedScore = (double) score;

        post.setScoreTop(calculatedScore);
        post.setScoreBest(calculatedScore);
        post.setScoreHot(calculatedScore);
        post.setScoreRising(calculatedScore);
    }
}
