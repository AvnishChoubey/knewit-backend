package com.knewit.backend.subreddit.repository;

import com.knewit.backend.subreddit.entity.SubredditJoinRequest;
import com.knewit.backend.subreddit.enums.SubredditJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubredditJoinRequestRepository
        extends JpaRepository<SubredditJoinRequest, Long> {

    Optional<SubredditJoinRequest>
    findBySubreddit_IdAndRequester_IdAndStatus(
            Long subredditId,
            Long requesterId,
            SubredditJoinRequestStatus status
    );
}