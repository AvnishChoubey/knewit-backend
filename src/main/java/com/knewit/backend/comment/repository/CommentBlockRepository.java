package com.knewit.backend.comment.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.comment.entity.CommentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentBlockRepository extends JpaRepository<CommentBlock, Long> {
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<CommentBlock> findByBlockerAndBlocked(User blocker, Comment blocked);

    List<CommentBlock> findAllByBlocker(User blocker);
}
