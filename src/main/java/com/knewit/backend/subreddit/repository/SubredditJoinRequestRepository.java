package com.knewit.backend.subreddit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.knewit.backend.subreddit.entity.SubredditJoinRequest;

@Repository
public interface SubredditJoinRequestRepository extends JpaRepository<SubredditJoinRequest, Long> {
    Optional<SubredditJoinRequest> findBySubredditIdAndRequesterIdAndStatus(Long subredditId, Long requesterId, String status);
}
