package com.knewit.backend.user.repository;

import com.knewit.backend.user.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    boolean existsByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
    long countByFollower_Id(Long followerId);
    long countByFollowed_Id(Long followedId);
    List<UserFollow> findAllByFollower_Id(Long followerId);
    Optional<UserFollow> findByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
    void deleteByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
}
