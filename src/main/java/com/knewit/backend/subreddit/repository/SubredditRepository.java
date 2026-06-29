
package com.knewit.backend.subreddit.repository;

import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.entity.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


@Repository
public interface SubredditRepository
        extends JpaRepository<Subreddit, Long> {



    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

    List<Subreddit> findByTopic(Topic topic);

    @Query("SELECT s FROM Subreddit s WHERE s.visibility = com.knewit.backend.subreddit.enums.Visibility.PUBLIC AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Subreddit> searchSubredditsFallback(@Param("query") String query);
}