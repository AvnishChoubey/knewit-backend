package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByComment_IdAndUser_Id(Long commentId, Long userId);
}