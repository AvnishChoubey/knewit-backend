package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.PostFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFollowRepository extends JpaRepository<PostFollow, Long> {

    Optional<PostFollow> findByFollower_IdAndFollowed_Id(Long userId, Long postId);

    boolean existsByFollower_IdAndFollowed_Id(Long userId, Long postId);

    List<PostFollow> findAllByFollower_Id(Long userId);
    void deleteByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
}