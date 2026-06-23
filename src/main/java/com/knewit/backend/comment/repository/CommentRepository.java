package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndCommentStatus(
            Long commentId,
            CommentStatus commentStatus
    );

    List<Comment> findAllByPost_IdAndCommentStatus(
            Long postId,
            CommentStatus commentStatus
    );

    List<Comment> findAllByParentComment_IdAndCommentStatus(
            Long parentCommentId,
            CommentStatus commentStatus
    );

    List<Comment> findAllByPost_IdAndParentCommentIsNullAndCommentStatus(
            Long postId,
            CommentStatus commentStatus
    );
}
