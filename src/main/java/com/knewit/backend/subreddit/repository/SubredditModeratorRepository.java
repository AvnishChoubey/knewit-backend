package com.knewit.backend.subreddit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SubredditModeratorRepository
        extends JpaRepository<SubredditModerator, Long> {

    Optional<SubredditModerator> findByUserAndSubreddit(
            User user,
            Subreddit subreddit
    );

    boolean existsByUserAndSubreddit(
            User user,
            Subreddit subreddit
    );

    List<SubredditModerator> findBySubreddit(
            Subreddit subreddit
    );
}