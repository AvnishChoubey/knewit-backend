package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.PostFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFollowRepository extends JpaRepository<PostFollow, Long> {

    Optional<PostFollow> findByFollowerIdAndFollowedId(Long userId, Long postId);

    boolean existsByFollwerIdAndFollwedId(Long userId, Long postId);

    List<PostFollow> findAllByFollowerId(Long userId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
}