
package com.knewit.backend.subreddit.repository;

import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.entity.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SubredditRepository
        extends JpaRepository<Subreddit, Long> {



    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

    List<Subreddit> findByTopic(Topic topic);

}