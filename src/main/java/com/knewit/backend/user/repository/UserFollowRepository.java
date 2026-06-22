package com.knewit.backend.user.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.user.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    long countByFollowerId(Long followerId);
    long countByFollowedId(Long followedId);
    List<UserFollow> findAllByFollower(User follower);
    Optional<UserFollow> findByFollowerAndFollowed(User follower, User followed);
}
