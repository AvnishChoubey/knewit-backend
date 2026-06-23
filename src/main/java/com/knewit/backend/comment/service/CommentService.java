package com.knewit.backend.comment.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.comment.dto.CommentDto;
import com.knewit.backend.comment.dto.CreateCommentRequest;
import com.knewit.backend.comment.dto.UpdateCommentRequest;
import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.entity.CommentSave;
import com.knewit.backend.comment.entity.CommentVote;
import com.knewit.backend.comment.enums.CommentStatus;
import com.knewit.backend.comment.repository.CommentRepository;
import com.knewit.backend.comment.repository.CommentSaveRepository;
import com.knewit.backend.comment.repository.CommentVoteRepository;
import com.knewit.backend.common.enums.VoteType;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final CommentSaveRepository commentSaveRepository;

    private CommentDto convertToDto(Comment comment, Long viewerId) {

        boolean saved = false;

        String votedState = "NONE";

        if (viewerId != null) {

            saved = commentSaveRepository.existsBySaverIdAndSavedId(viewerId, comment.getId());

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
    public List<CommentDto> getCommentsForPost(Long postId, Long viewerId) {
        return commentRepository.findAllByPost_IdAndCommentStatus(postId, CommentStatus.PUBLISHED)
                .stream()
                .map(comment -> convertToDto(comment, viewerId))
                .toList();
    }

    @Transactional
    public void voteComment(Long userId, Long commentId, VoteType voteType) {
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
    public void toggleSaveComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new RuntimeException("Comment not found"));

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        CommentSave existingSave = commentSaveRepository.findBySaverIdAndSavedId(userId, commentId).orElse(null);

        if (existingSave != null) {
            commentSaveRepository.delete(existingSave);
            return;
        }

        CommentSave save = CommentSave.builder()
                        .saved(comment)
                        .saver(user)
                        .build();

        commentSaveRepository.save(save);
    }

    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
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

        return convertToDto(comment, userId);
    }

    private void softDeleteChildren(Long parentCommentId) {
        var children = commentRepository.findAllByParentComment_IdAndCommentStatus(parentCommentId, CommentStatus.PUBLISHED);

        for (Comment child : children) {
            child.setCommentStatus(CommentStatus.REMOVED);
            commentRepository.save(child);
            softDeleteChildren(child.getId());
        }
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only author can delete comment");
        }

        comment.setCommentStatus(CommentStatus.REMOVED);

        commentRepository.save(comment);

        softDeleteChildren(comment.getId());
    }

    @Transactional
    public CommentDto createComment(Long authorId, Long postId, CreateCommentRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        Comment parentComment = null;

        int depth = 0;

        if (request.getParentCommentId() != null) {

            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new RuntimeException("Parent comment belongs to another post");
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

        CommentVote selfVote = CommentVote.builder()
                        .comment(comment)
                        .user(author)
                        .voteType(VoteType.UPVOTE)
                        .build();

        commentVoteRepository.save(selfVote);

        post.setCommentCount(post.getCommentCount() + 1);

        postRepository.save(post);

        return convertToDto(comment, authorId);
    }
}