package com.knewit.backend.post.service;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.enums.VoteType;
import com.knewit.backend.common.exception.KnewitException;
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
import com.knewit.backend.search.entity.PostDocument;
import com.knewit.backend.search.service.SearchService;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.enums.MemberStatus;
import com.knewit.backend.subreddit.enums.PostingPolicy;
import com.knewit.backend.subreddit.enums.Visibility;
import com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import com.knewit.backend.user.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.knewit.backend.post.dto.VotePostRequest;

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
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private PostBlockRepository postBlockRepository;
    @Autowired private SearchService searchService;

    @Transactional
    public PostDto createPost(CustomUserDetails customUserDetails, CreatePostRequest request) {

        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long authorId = customUserDetails.getUserId();

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

        PostDocument postDocument = postToPostDocument(post);
        searchService.enqueueSyncEvent("POST", post.getId().toString(), "CREATE", postDocument);

        return postToPostDto(
                post,
                author.getId()
        );
    }

    @Transactional
    public PostDto updatePost(Long postId, CustomUserDetails customUserDetails, UpdatePostRequest request) {

        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long authorId = customUserDetails.getUserId();

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

        PostDocument postDocument = postToPostDocument(post);
        searchService.enqueueSyncEvent("POST", post.getId().toString(), "UPDATE", postDocument);

        return postToPostDto(post, authorId);
    }

    @Transactional(readOnly = true)
    public PostDto getPost(CustomUserDetails customUserDetails, Long postId) {

        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L;

        Post post = postRepository.findByIdAndPostStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (viewerId != 0L) {
            if (userBlockRepository.findByBlocker_IdAndBlocked_Id(viewerId, post.getAuthor().getId()).isPresent()) {
                throw new KnewitException("USER_BLOCKED", "Post owner has been blocked by you.", HttpStatus.BAD_REQUEST);
            }

            if (userBlockRepository.findByBlocker_IdAndBlocked_Id(post.getAuthor().getId(), viewerId).isPresent()) {
                throw new KnewitException("USER_BLOCKED", "Post owner has blocked you.", HttpStatus.BAD_REQUEST);
            }

            if (postBlockRepository.findByBlocker_IdAndBlocked_Id(viewerId, postId).isPresent()) {
                throw new KnewitException("POST_BLOCKED", "Post has been blocked by you.", HttpStatus.BAD_REQUEST);
            }
        }

        return postToPostDto(post, viewerId);
    }

    @Transactional
    public void deletePost(Long postId, CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long authorId = customUserDetails.getUserId();
        Post post = postRepository.findByIdAndAuthor_Id(postId, authorId)
                .orElseThrow(() -> new RuntimeException("Post not found or unauthorized"));

        post.setPostStatus(PostStatus.ARCHIVED);

        PostDocument postDocument = postToPostDocument(post);
        searchService.enqueueSyncEvent("POST", post.getId().toString(), "DELETE", postDocument);

        postRepository.save(post);
    }

    @Transactional
    public PostDto votePost(Long postId, CustomUserDetails customUserDetails, VotePostRequest request) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();

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

        return postToPostDto(post, userId);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String keyword, CustomUserDetails customUserDetails, int page, int size) {
        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepository.searchPosts(keyword, PostStatus.PUBLISHED, pageable)
                .map(post -> postToPostDto(post, viewerId));
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getFeed(Long viewerId, int page, int size, String sort, String feedType) {

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

        Page<Post> posts;
        if ("HOME".equalsIgnoreCase(feedType) && viewerId != null && viewerId != 0L) {
            List<Long> joinedSubredditIds = subredditMemberRepository
                    .findByUser_IdAndMemberStatus(viewerId, MemberStatus.APPROVED)
                    .stream()
                    .map(m -> m.getSubreddit().getId())
                    .collect(Collectors.toList());

            if (!joinedSubredditIds.isEmpty()) {
                posts = postRepository.findBySubreddit_IdInAndPostStatus(joinedSubredditIds, PostStatus.PUBLISHED, pageable);
            } else {
                posts = postRepository.findByPostStatus(PostStatus.PUBLISHED, pageable);
            }
        } else {
            posts = postRepository.findByPostStatus(PostStatus.PUBLISHED, pageable);
        }

        return posts.map(post ->
                postToPostDto(
                        post,
                        viewerId
                ));
    }

    private PostDto postToPostDto(Post post, Long viewerId) {

        boolean followed = false;

        if (viewerId != null && viewerId != 0L) {
            followed =
                    postFollowRepository
                            .existsByFollower_IdAndFollowed_Id(viewerId, post.getId());
        }

        boolean saved = false;

        if (viewerId != null && viewerId != 0L) {
            saved = postSaveRepository
                    .existsBySaver_IdAndSaved_Id(viewerId, post.getId());
        }

        String votedState = "NONE";

        if (viewerId != null && viewerId != 0L) {
            PostVote vote = postVoteRepository
                    .findByPost_IdAndUser_Id(
                            post.getId(),
                            viewerId
                    )
                    .orElse(null);

            if (vote != null) {
                votedState = vote.getVoteType().name();
            }
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

        long score = post.getUpvoteCount() - post.getDownvoteCount();

        double calculatedScore = (double) score;

        post.setScoreTop(calculatedScore);
        post.setScoreBest(calculatedScore);
        post.setScoreHot(calculatedScore);
        post.setScoreRising(calculatedScore);
    }

    private PostDocument postToPostDocument(Post post) {
        return PostDocument.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .body(post.getBody())
                .subreddit(post.getSubreddit().getName())
                .authorUsername(post.getAuthor().getUsername())
                .postStatus(post.getPostStatus().name())
                .visibility(post.getSubreddit().getVisibility().name())
                .build();
    }
}