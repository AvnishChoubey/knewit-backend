package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.CommentFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentFollowRepository extends JpaRepository<CommentFollow, Long> {
    List<CommentFollow> findAllByFollowerId(Long userId);
    Optional<CommentFollow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
}
