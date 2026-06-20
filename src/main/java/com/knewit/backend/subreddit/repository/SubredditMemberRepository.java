package com.knewit.backend.subreddit.repository;

import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.entity.SubredditMember;
import com.knewit.backend.subreddit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubredditMemberRepository
        extends JpaRepository<SubredditMember, Long> {

    boolean existsByUserAndSubreddit(
            User user,
            Subreddit subreddit
    );

    Optional<SubredditMember> findByUserAndSubreddit(
            User user,
            Subreddit subreddit
    );

    long countBySubreddit(
            Subreddit subreddit
    );
}