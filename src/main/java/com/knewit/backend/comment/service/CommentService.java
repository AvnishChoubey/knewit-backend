package com.knewit.backend.comment.service;

import com.knewit.backend.auth.dto.CustomUserDetails;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.comment.dto.CommentDto;
import com.knewit.backend.comment.dto.CreateCommentRequest;
import com.knewit.backend.comment.dto.UpdateCommentRequest;
import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.entity.CommentVote;
import com.knewit.backend.comment.enums.CommentStatus;
import com.knewit.backend.comment.repository.CommentRepository;
import com.knewit.backend.comment.repository.CommentSaveRepository;
import com.knewit.backend.comment.repository.CommentVoteRepository;
import com.knewit.backend.common.enums.VoteType;
import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.repository.PostRepository;
import com.knewit.backend.search.entity.CommentDocument;
import com.knewit.backend.search.service.SearchService;
import com.knewit.backend.user.repository.UserBlockRepository;
import com.knewit.backend.comment.repository.CommentBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private static final int MAX_DEPTH = 7;

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    @Autowired private SearchService searchService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final CommentSaveRepository commentSaveRepository;
    @Autowired private UserBlockRepository userBlockRepository;
    @Autowired private CommentBlockRepository commentBlockRepository;

    private CommentDto commentToCommentDto(Comment comment, Long viewerId) {

        boolean saved = false;

        String votedState = "NONE";

        if (viewerId != null) {

            saved = commentSaveRepository.existsBySaver_IdAndSaved_Id(viewerId, comment.getId());

            CommentVote vote = commentVoteRepository.findByComment_IdAndUser_Id(comment.getId(), viewerId).orElse(null);

            if (vote != null) {
                votedState = vote.getVoteType().name();
            }
        }

        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .parentCommentId(
                        comment.getParentComment() != null
                                ? comment.getParentComment().getId()
                                : null
                )
                .depthLevel(comment.getDepthLevel())
                .body(comment.getBody())
                .commentStatus(comment.getCommentStatus().name())
                .upvoteCount(comment.getUpvoteCount())
                .downvoteCount(comment.getDownvoteCount())
                .shareCount(comment.getShareCount())
                .createdAt(comment.getCreatedAt().toString())
                .saved(saved)
                .votedState(votedState)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForPost(CustomUserDetails customUserDetails, Long postId) {
        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L;

        // Resolve viewer entity once for CommentBlockRepository calls (needs User object)
        User viewer = (viewerId != null && viewerId != 0L)
                ? userRepository.findById(viewerId).orElse(null)
                : null;

        return commentRepository.findAllByPost_IdAndCommentStatus(postId, CommentStatus.PUBLISHED)
                .stream()
                .filter(comment -> {
                    if (viewer == null) return true;

                    Long commentAuthorId = comment.getAuthor().getId();
                    if (viewerId.equals(commentAuthorId)) return true;

                    // Viewer blocked the comment author
                    if (userBlockRepository.findByBlocker_IdAndBlocked_Id(viewerId, commentAuthorId).isPresent()) {
                        return false;
                    }

                    // Comment author blocked the viewer
                    if (userBlockRepository.findByBlocker_IdAndBlocked_Id(commentAuthorId, viewerId).isPresent()) {
                        return false;
                    }

                    // Viewer blocked the comment itself
                    if (commentBlockRepository.findByBlockerAndBlocked(viewer, comment).isPresent()) {
                        return false;
                    }

                    return true;
                })
                .map(comment -> commentToCommentDto(comment, viewerId))
                .toList();
    }

    @Transactional
    public void voteComment(CustomUserDetails customUserDetails, Long commentId, VoteType voteType) {

        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();

        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Comment not found"
                        ));

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        CommentVote existingVote =
                commentVoteRepository
                        .findByComment_IdAndUser_Id(
                                commentId,
                                userId
                        )
                        .orElse(null);

        if (existingVote == null) {

            CommentVote vote =
                    CommentVote.builder()
                            .comment(comment)
                            .user(user)
                            .voteType(voteType)
                            .build();

            commentVoteRepository.save(vote);

            if (voteType == VoteType.UPVOTE) {
                comment.setUpvoteCount(
                        comment.getUpvoteCount() + 1
                );
            } else {
                comment.setDownvoteCount(
                        comment.getDownvoteCount() + 1
                );
            }
        }

        else if (existingVote.getVoteType()
                == voteType) {

            commentVoteRepository
                    .delete(existingVote);

            if (voteType == VoteType.UPVOTE) {
                comment.setUpvoteCount(
                        comment.getUpvoteCount() - 1
                );
            } else {
                comment.setDownvoteCount(
                        comment.getDownvoteCount() - 1
                );
            }
        }

        else {

            if (existingVote.getVoteType()
                    == VoteType.UPVOTE) {

                comment.setUpvoteCount(
                        comment.getUpvoteCount() - 1
                );

                comment.setDownvoteCount(
                        comment.getDownvoteCount() + 1
                );
            }

            else {

                comment.setDownvoteCount(
                        comment.getDownvoteCount() - 1
                );

                comment.setUpvoteCount(
                        comment.getUpvoteCount() + 1
                );
            }

            existingVote.setVoteType(
                    voteType
            );

            commentVoteRepository
                    .save(existingVote);
        }

        commentRepository.save(comment);
    }

    @Transactional
    public CommentDto updateComment(CustomUserDetails customUserDetails, Long commentId, UpdateCommentRequest request) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only author can update comment");
        }

        if (comment.getCommentStatus() == CommentStatus.REMOVED) {
            throw new RuntimeException("Removed comment cannot be updated");
        }

        comment.setBody(request.getBody());

        commentRepository.save(comment);

        CommentDocument commentDocument = commentToCommentDocument(comment);

        searchService.enqueueSyncEvent("COMMENT", comment.getId().toString(), "UPDATE", commentDocument);

        return commentToCommentDto(comment, userId);
    }

    private void softDeleteChildren(Long parentCommentId) {
        var children = commentRepository.findAllByParentComment_IdAndCommentStatus(parentCommentId, CommentStatus.PUBLISHED);

        for (Comment child : children) {
            child.setCommentStatus(CommentStatus.REMOVED);
            commentRepository.save(child);
            CommentDocument commentDocument = CommentDocument.builder()
                    .id(child.getId().toString())
                    .body(child.getBody())
                    .postId(child.getPost().getId().toString())
                    .authorUsername(child.getAuthor().getUsername())
                    .contentStatus(child.getCommentStatus().toString())
                    .build();

            searchService.enqueueSyncEvent("COMMENT", child.getId().toString(), "DELETE", commentDocument);
            softDeleteChildren(child.getId());
        }
    }

    @Transactional
    public void deleteComment(CustomUserDetails customUserDetails, Long commentId) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only author can delete comment");
        }

        comment.setCommentStatus(CommentStatus.REMOVED);

        commentRepository.save(comment);

        CommentDocument commentDocument = commentToCommentDocument(comment);

        searchService.enqueueSyncEvent("COMMENT", comment.getId().toString(), "DELETE", commentDocument);

        softDeleteChildren(comment.getId());
    }

    @Transactional
    public CommentDto createComment(CustomUserDetails customUserDetails, Long postId, CreateCommentRequest request) {

        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long authorId = customUserDetails.getUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ban rules: check blocks between commenter and post author
        Long postAuthorId = post.getAuthor().getId();
        if (!postAuthorId.equals(authorId)) {
            if (userBlockRepository.findByBlocker_IdAndBlocked_Id(authorId, postAuthorId).isPresent()) {
                throw new KnewitException("USER_BLOCKED", "You have blocked the post owner.", HttpStatus.BAD_REQUEST);
            }
            if (userBlockRepository.findByBlocker_IdAndBlocked_Id(postAuthorId, authorId).isPresent()) {
                throw new KnewitException("USER_BLOCKED", "The post owner has blocked you.", HttpStatus.BAD_REQUEST);
            }
        }

        Comment parentComment = null;

        int depth = 0;

        if (request.getParentCommentId() != null) {

            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new RuntimeException("Parent comment belongs to another post");
            }

            // Ban rules: check blocks between commenter and parent comment author
            Long parentCommentAuthorId = parentComment.getAuthor().getId();
            if (!parentCommentAuthorId.equals(authorId)) {
                if (userBlockRepository.findByBlocker_IdAndBlocked_Id(authorId, parentCommentAuthorId).isPresent()) {
                    throw new KnewitException("USER_BLOCKED", "You have blocked the parent comment owner.", HttpStatus.BAD_REQUEST);
                }
                if (userBlockRepository.findByBlocker_IdAndBlocked_Id(parentCommentAuthorId, authorId).isPresent()) {
                    throw new KnewitException("USER_BLOCKED", "The parent comment owner has blocked you.", HttpStatus.BAD_REQUEST);
                }
            }

            depth = parentComment.getDepthLevel() + 1;

            if (depth > MAX_DEPTH) {
                throw new RuntimeException("Maximum comment depth exceeded");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .parentComment(parentComment)
                .depthLevel(depth)
                .body(request.getBody())
                .commentStatus(CommentStatus.PUBLISHED)
                .upvoteCount(1L)
                .downvoteCount(0L)
                .shareCount(0L)
                .reportCount(0L)
                .build();

        comment = commentRepository.save(comment);

        CommentDocument commentDocument = commentToCommentDocument(comment);

        searchService.enqueueSyncEvent("COMMENT", comment.getId().toString(), "CREATE", commentDocument);

        CommentVote selfVote = CommentVote.builder()
                .comment(comment)
                .user(author)
                .voteType(VoteType.UPVOTE)
                .build();

        commentVoteRepository.save(selfVote);

        post.setCommentCount(post.getCommentCount() + 1);

        postRepository.save(post);

        return commentToCommentDto(comment, authorId);
    }

    private CommentDocument commentToCommentDocument(Comment comment) {
        return CommentDocument.builder()
                .id(comment.getId().toString())
                .body(comment.getBody())
                .postId(comment.getPost().getId().toString())
                .authorUsername(comment.getAuthor().getUsername())
                .contentStatus(comment.getCommentStatus().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long authorId, CustomUserDetails customUserDetails) {
        Long viewerId = (customUserDetails != null) ? customUserDetails.getUserId() : 0L;
        if (viewerId != 0L && !authorId.equals(viewerId) && userBlockRepository.findByBlocker_IdAndBlocked_Id(authorId, viewerId).isPresent()) {
            throw new KnewitException("ALREADY_BLOCKED", "You are blocked", HttpStatus.BAD_REQUEST);
        }

        return commentRepository.findByAuthor_IdAndCommentStatusOrderByCreatedAtDesc(authorId, CommentStatus.PUBLISHED)
                .stream()
                .map(comment -> commentToCommentDto(comment, viewerId))
                .toList();
    }
}