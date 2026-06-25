package com.knewit.backend.search.repository;

import com.knewit.backend.search.entity.SubredditDocument;
import com.knewit.backend.subreddit.entity.Subreddit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SubredditSearchRepository extends ElasticsearchRepository<SubredditDocument, String> {
    List<SubredditDocument> findByNameContaining(String name);
}
