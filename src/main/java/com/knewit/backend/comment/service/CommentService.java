package com.knewit.backend.comment.service;

import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.user.entity.User;
import com.knewit.backend.comment.repository.CommentRepository;
import com.knewit.backend.post.repository.PostRepository;
import com.knewit.backend.user.repository.UserRepository;
import com.knewit.backend.comment.request.CreateCommentRequest;
import com.knewit.backend.comment.request.UpdateCommentRequest;
import com.knewit.backend.comment.response.CommentResponse;
import com.knewit.backend.comment.transformer.CommentTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(user)
                .post(post)
                .build();

        comment = commentRepository.save(comment);

        return CommentTransformer.toResponse(comment);
    }

    public CommentResponse replyToComment(
            Long userId,
            Long commentId,
            CreateCommentRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found"));

        Comment reply = Comment.builder()
                .content(request.getContent())
                .author(user)
                .post(parentComment.getPost())
                .parentComment(parentComment)
                .build();

        reply = commentRepository.save(reply);

        return CommentTransformer.toResponse(reply);
    }

    public List<CommentResponse> getCommentsByPost(
            Long postId
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        return commentRepository.findByPost(post)
                .stream()
                .filter(comment ->
                        comment.getParentComment() == null
                )
                .filter(comment ->
                        !Boolean.TRUE.equals(comment.getDeleted())
                )
                .map(CommentTransformer::toResponse)
                .toList();
    }

    public CommentResponse updateComment(
            Long commentId,
            UpdateCommentRequest request
    ) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found"));

        comment.setContent(request.getContent());
        comment.setEdited(true);

        comment = commentRepository.save(comment);

        return CommentTransformer.toResponse(comment);
    }

    public void deleteComment(
            Long commentId
    ) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found"));

        comment.setDeleted(true);

        commentRepository.save(comment);
    }
}