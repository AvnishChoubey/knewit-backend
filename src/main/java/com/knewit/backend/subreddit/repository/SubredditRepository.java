
package com.knewit.backend.subreddit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SubredditRepository
        extends JpaRepository<Subreddit, Long> {

    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);
}