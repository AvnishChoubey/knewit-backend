package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndCommentStatus(Long commentId, CommentStatus commentStatus);
    List<Comment> findAllByPost_IdAndCommentStatus(Long postId, CommentStatus commentStatus);
    List<Comment> findAllByParentComment_IdAndCommentStatus(Long parentCommentId, CommentStatus commentStatus);
    List<Comment> findAllByPost_IdAndParentCommentIsNullAndCommentStatus(Long postId, CommentStatus commentStatus);

    @Query("SELECT COALESCE(SUM(c.upvoteCount - c.downvoteCount), 0) FROM Comment c WHERE c.author.id = :authorId AND c.commentStatus = com.knewit.backend.comment.enums.CommentStatus.PUBLISHED")
    long sumCommentScoreByAuthorId(@Param("authorId") Long authorId);

    int countByAuthor_IdAndCommentStatus(Long id, CommentStatus commentStatus);

    @Query("SELECT c FROM Comment c WHERE c.commentStatus = com.knewit.backend.comment.enums.CommentStatus.PUBLISHED AND LOWER(c.body) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Comment> searchCommentsFallback(@Param("query") String query);
}