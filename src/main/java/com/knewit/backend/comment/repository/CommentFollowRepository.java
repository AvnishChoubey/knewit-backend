package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.CommentFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentFollowRepository extends JpaRepository<CommentFollow, Long> {
    List<CommentFollow> findAllByFollower_Id(Long userId);
    Optional<CommentFollow> findByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
    void deleteByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
}
