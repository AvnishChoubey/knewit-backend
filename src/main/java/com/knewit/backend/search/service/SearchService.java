package com.knewit.backend.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knewit.backend.config.ElasticsearchConfig;
import com.knewit.backend.search.dto.SearchResponseDto;
import com.knewit.backend.search.entity.*;
import com.knewit.backend.search.repository.SearchIndexSyncEventRepository;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import com.knewit.backend.post.repository.PostRepository;
import com.knewit.backend.comment.repository.CommentRepository;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    @Autowired private ElasticsearchClient elasticsearchClient;
    @Autowired private ElasticsearchConfig elasticsearchConfig;
    @Autowired private SearchIndexSyncEventRepository outboxRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private SubredditRepository subredditRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;

    private String getIndexName(String suffix) {
        String prefix = elasticsearchConfig.getIndexPrefix();
        if (prefix == null || prefix.isBlank()) {
            return suffix;
        }
        return prefix + "_" + suffix;
    }

    @PostConstruct
    public void initIndices() {
        try {
            createIndexIfNotExists("users");
            createIndexIfNotExists("subreddits");
            createIndexIfNotExists("posts");
            createIndexIfNotExists("comments");
        } catch (Exception e) {
            log.warn("Could not connect to Elasticsearch to initialize indices: {}", e.getMessage());
        }
    }

    private void createIndexIfNotExists(String type) throws IOException {
        String indexName = getIndexName(type);
        boolean exists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
        if (!exists) {
            elasticsearchClient.indices().create(c -> c.index(indexName));
            log.info("Created Elasticsearch index: {}", indexName);
        }
    }

    @Transactional(readOnly = true)
    public SearchResponseDto search(String query) {
        try {
            elasticsearchClient.ping();
        } catch (Exception e) {
            log.warn("Elasticsearch ping failed, falling back to database search. Error: {}", e.getMessage());
            return searchDbFallback(query);
        }

        // wildcard needs lowercase to match against lowercased field values
        String wildcardQuery = "*" + query.toLowerCase() + "*";

        try {
            // 1. Search Users
            List<SearchResponseDto.UserResultDto> users = new ArrayList<>();
            try {
                SearchResponse<UserDocument> userResponse = elasticsearchClient.search(s -> s
                        .index(getIndexName("users"))
                        .query(q -> q
                                .wildcard(w -> w
                                        .field("username")
                                        .value(wildcardQuery)
                                )
                        ), UserDocument.class);

                for (Hit<UserDocument> hit : userResponse.hits().hits()) {
                    UserDocument doc = hit.source();
                    if (doc != null) {
                        users.add(new SearchResponseDto.UserResultDto(doc.getUsername()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed user search query", e);
            }

            // 2. Search Subreddits
            List<SearchResponseDto.SubredditResultDto> subreddits = new ArrayList<>();
            try {
                SearchResponse<SubredditDocument> subredditResponse = elasticsearchClient.search(s -> s
                        .index(getIndexName("subreddits"))
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .bool(inner -> inner
                                                        .should(sh -> sh
                                                                .wildcard(w -> w
                                                                        .field("name")
                                                                        .value(wildcardQuery)
                                                                )
                                                        )
                                                        .should(sh -> sh
                                                                .wildcard(w -> w
                                                                        .field("title")
                                                                        .value(wildcardQuery)
                                                                )
                                                        )
                                                        .minimumShouldMatch("1")
                                                )
                                        )
                                        .filter(f -> f
                                                .term(t -> t
                                                        .field("visibility")
                                                        .value("PUBLIC")
                                                )
                                        )
                                )
                        ), SubredditDocument.class);

                for (Hit<SubredditDocument> hit : subredditResponse.hits().hits()) {
                    SubredditDocument doc = hit.source();
                    if (doc != null) {
                        subreddits.add(new SearchResponseDto.SubredditResultDto(doc.getName()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed subreddit search query", e);
            }

            // 3. Search Posts
            List<SearchResponseDto.PostResultDto> posts = new ArrayList<>();
            try {
                SearchResponse<PostDocument> postResponse = elasticsearchClient.search(s -> s
                        .index(getIndexName("posts"))
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .bool(inner -> inner
                                                        .should(sh -> sh
                                                                .wildcard(w -> w
                                                                        .field("title")
                                                                        .value(wildcardQuery)
                                                                )
                                                        )
                                                        .should(sh -> sh
                                                                .wildcard(w -> w
                                                                        .field("body")
                                                                        .value(wildcardQuery)
                                                                )
                                                        )
                                                        .minimumShouldMatch("1")
                                                )
                                        )
                                        .filter(f -> f
                                                .term(t -> t
                                                        .field("postStatus")
                                                        .value("PUBLISHED")
                                                )
                                        )
                                        .filter(f -> f
                                                .term(t -> t
                                                        .field("visibility")
                                                        .value("PUBLIC")
                                                )
                                        )
                                )
                        ), PostDocument.class);

                for (Hit<PostDocument> hit : postResponse.hits().hits()) {
                    PostDocument doc = hit.source();
                    if (doc != null) {
                        posts.add(new SearchResponseDto.PostResultDto(doc.getId(), doc.getTitle(), doc.getAuthorUsername()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed post search query", e);
            }

            // 4. Search Comments
            List<SearchResponseDto.CommentResultDto> comments = new ArrayList<>();
            try {
                SearchResponse<CommentDocument> commentResponse = elasticsearchClient.search(s -> s
                        .index(getIndexName("comments"))
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .wildcard(w -> w
                                                        .field("body")
                                                        .value(wildcardQuery)
                                                )
                                        )
                                        .filter(f -> f
                                                .term(t -> t
                                                        .field("contentStatus")
                                                        .value("PUBLISHED")
                                                )
                                        )
                                )
                        ), CommentDocument.class);

                for (Hit<CommentDocument> hit : commentResponse.hits().hits()) {
                    CommentDocument doc = hit.source();
                    if (doc != null) {
                        comments.add(new SearchResponseDto.CommentResultDto(doc.getId(), doc.getBody(), doc.getPostId(), doc.getAuthorUsername()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed comment search query", e);
            }

            SearchResponseDto.ResultsDto results = new SearchResponseDto.ResultsDto(users, subreddits, posts, comments);
            return new SearchResponseDto(query, results);

        } catch (Exception e) {
            log.warn("Search request failed, falling back to database search. Error: {}", e.getMessage());
            return searchDbFallback(query);
        }
    }

    private SearchResponseDto searchDbFallback(String query) {
        log.info("Executing database fallback search for query: {}", query);

        // 1. Search Users
        List<SearchResponseDto.UserResultDto> users = userRepository.searchUsersFallback(query).stream()
                .map(u -> new SearchResponseDto.UserResultDto(u.getUsername()))
                .limit(20)
                .collect(Collectors.toList());

        // 2. Search Subreddits
        List<SearchResponseDto.SubredditResultDto> subreddits = subredditRepository.searchSubredditsFallback(query).stream()
                .map(s -> new SearchResponseDto.SubredditResultDto(s.getName()))
                .limit(20)
                .collect(Collectors.toList());

        // 3. Search Posts
        org.springframework.data.domain.Page<com.knewit.backend.post.entity.Post> postPage =
                postRepository.searchPosts(query, com.knewit.backend.post.enums.PostStatus.PUBLISHED, PageRequest.of(0, 20));

        List<SearchResponseDto.PostResultDto> posts = postPage.getContent().stream()
                .filter(p -> p.getSubreddit() != null && p.getSubreddit().getVisibility() == com.knewit.backend.subreddit.enums.Visibility.PUBLIC)
                .map(p -> new SearchResponseDto.PostResultDto(
                        p.getId().toString(),
                        p.getTitle(),
                        p.getAuthor() != null ? p.getAuthor().getUsername() : "anonymous"
                ))
                .collect(Collectors.toList());

        // 4. Search Comments
        List<SearchResponseDto.CommentResultDto> comments = commentRepository.searchCommentsFallback(query).stream()
                .filter(c -> c.getPost() != null
                        && c.getPost().getPostStatus() == com.knewit.backend.post.enums.PostStatus.PUBLISHED
                        && c.getPost().getSubreddit() != null
                        && c.getPost().getSubreddit().getVisibility() == com.knewit.backend.subreddit.enums.Visibility.PUBLIC)
                .map(c -> new SearchResponseDto.CommentResultDto(
                        c.getId().toString(),
                        c.getBody(),
                        c.getPost().getId().toString(),
                        c.getAuthor() != null ? c.getAuthor().getUsername() : "anonymous"
                ))
                .limit(20)
                .collect(Collectors.toList());

        SearchResponseDto.ResultsDto results = new SearchResponseDto.ResultsDto(users, subreddits, posts, comments);
        return new SearchResponseDto(query, results);
    }

    @Transactional
    public void enqueueSyncEvent(String entityType, String entityId, String operation, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            SearchIndexSyncEvent event = SearchIndexSyncEvent.builder()
                    .id(UUID.randomUUID())
                    .entityType(entityType)
                    .entityId(entityId)
                    .operation(operation)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .build();
            outboxRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to enqueue search index sync event", e);
            throw new RuntimeException("Failed to enqueue sync event", e);
        }
    }

    public void processSyncEvent(SearchIndexSyncEvent event) throws Exception {

        String indexName = getIndexName(event.getEntityType().toLowerCase() + "s");

        if ("DELETE".equalsIgnoreCase(event.getOperation())) {
            elasticsearchClient.delete(d -> d
                    .index(indexName)
                    .id(event.getEntityId())
            );
            return;
        }

        switch (event.getEntityType().toUpperCase()) {

            case "USER" -> {
                UserDocument doc = objectMapper.readValue(event.getPayload(), UserDocument.class);
                elasticsearchClient.index(i -> i
                        .index(indexName)
                        .id(doc.getId())
                        .document(doc)
                );
            }

            case "POST" -> {
                PostDocument doc = objectMapper.readValue(event.getPayload(), PostDocument.class);
                elasticsearchClient.index(i -> i
                        .index(indexName)
                        .id(doc.getId())
                        .document(doc)
                );
            }

            case "SUBREDDIT" -> {
                SubredditDocument doc = objectMapper.readValue(event.getPayload(), SubredditDocument.class);
                elasticsearchClient.index(i -> i
                        .index(indexName)
                        .id(doc.getId())
                        .document(doc)
                );
            }

            case "COMMENT" -> {
                CommentDocument doc = objectMapper.readValue(event.getPayload(), CommentDocument.class);
                elasticsearchClient.index(i -> i
                        .index(indexName)
                        .id(doc.getId())
                        .document(doc)
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unknown entity type: " + event.getEntityType()
            );
        }
    }
}