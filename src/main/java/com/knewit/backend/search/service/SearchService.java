package com.knewit.backend.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.search.dto.SearchResponseDto;
import com.knewit.backend.search.entity.*;
import com.knewit.backend.search.repository.SearchIndexSyncEventRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    @Autowired private ElasticsearchClient elasticsearchClient;
    @Autowired private SearchIndexSyncEventRepository outboxRepository;
    @Autowired private ObjectMapper objectMapper;

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

    private void createIndexIfNotExists(String indexName) throws IOException {
        boolean exists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
        if (!exists) {
            elasticsearchClient.indices().create(c -> c.index(indexName));
            log.info("Created Elasticsearch index: {}", indexName);
        }
    }

    @Transactional
    public void enqueueSyncEvent(String entityType, Long entityId, String operation, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            SearchIndexSyncEvent event = SearchIndexSyncEvent.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .operation(operation)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            outboxRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to enqueue search index sync event", e);
            throw new RuntimeException("Failed to enqueue sync event", e);
        }
    }

    public void processSyncEvent(SearchIndexSyncEvent event) throws Exception {

        String indexName = event.getEntityType().toLowerCase() + "s";

        if ("DELETE".equalsIgnoreCase(event.getOperation())) {

            elasticsearchClient.delete(d -> d
                    .index(indexName)
                    .id(event.getEntityId().toString())
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

    public SearchResponseDto search(String query) {

        try {
            elasticsearchClient.ping();
        } catch (Exception e) {
            throw new KnewitException("SEARCH_UNAVAILABLE", "Elasticsearch cluster is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }

        String normalizedQuery = query.toLowerCase();

        List<SearchResponseDto.UserResultDto> users = new ArrayList<>();

        try {
            SearchResponse<UserDocument> userResponse =
                    elasticsearchClient.search(s -> s
                                    .index("users")
                                    .query(q -> q
                                            .wildcard(w -> w
                                                    .field("username.keyword")
                                                    .value(normalizedQuery + "*")
                                            )
                                    ),
                            UserDocument.class
                    );

            for (Hit<UserDocument> hit : userResponse.hits().hits()) {
                if (hit.source() != null) {
                    users.add(new SearchResponseDto.UserResultDto(
                            hit.source().getUsername()
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed user search query", e);
        }

        List<SearchResponseDto.SubredditResultDto> subreddits = new ArrayList<>();

        try {
            SearchResponse<SubredditDocument> subredditResponse =
                    elasticsearchClient.search(s -> s
                                    .index("subreddits")
                                    .query(q -> q
                                        .bool(b -> b
                                            .must(m -> m
                                                .multiMatch(mm -> mm
                                                    .query(query)
                                                    .fields("name^5", "topic^4")
                                                    .type(TextQueryType.BestFields)
                                                    .operator(Operator.Or)
                                                )
                                            )
                                        )
                                    ),
                            SubredditDocument.class
                    );

            for (Hit<SubredditDocument> hit : subredditResponse.hits().hits()) {
                if (hit.source() != null) {
                    subreddits.add(new SearchResponseDto.SubredditResultDto(
                            hit.source().getTitle()
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed subreddit search query", e);
        }

        List<SearchResponseDto.PostResultDto> posts = new ArrayList<>();

        try {
            SearchResponse<PostDocument> postResponse =
                    elasticsearchClient.search(s -> s
                                    .index("posts")
                                    .query(q -> q
                                        .bool(b -> b
                                            .must(m -> m
                                                .multiMatch(mm -> mm
                                                    .query(query)
                                                    .fields("body^3")
                                                    .type(TextQueryType.BestFields)
                                                    .operator(Operator.Or)
                                                )
                                            )
                                            .filter(f -> f
                                                .term(t -> t
                                                    .field("postStatus.keyword")
                                                    .value("PUBLISHED")
                                                )
                                            )
                                        )
                                    ),
                            PostDocument.class
                    );

            for (Hit<PostDocument> hit : postResponse.hits().hits()) {
                if (hit.source() != null) {
                    posts.add(new SearchResponseDto.PostResultDto(
                            hit.source().getId(),
                            hit.source().getTitle(),
                            hit.source().getAuthorUsername()
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed post search query", e);
        }

        List<SearchResponseDto.CommentResultDto> comments = new ArrayList<>();

        try {
            SearchResponse<CommentDocument> commentResponse =
                    elasticsearchClient.search(s -> s
                                    .index("comments")
                                    .query(q -> q
                                        .bool(b -> b
                                            .must(m -> m
                                                .multiMatch(mm -> mm
                                                    .query(query)
                                                    .fields("body")
                                                    .type(TextQueryType.BestFields)
                                                    .operator(Operator.Or)
                                                )
                                            )
                                            .filter(f -> f
                                                .term(t -> t
                                                    .field("contentStatus.keyword")
                                                    .value("PUBLISHED")
                                                )
                                            )
                                        )
                                    ),
                            CommentDocument.class
                    );

            for (Hit<CommentDocument> hit : commentResponse.hits().hits()) {
                if (hit.source() != null) {
                    comments.add(new SearchResponseDto.CommentResultDto(
                            hit.source().getId(),
                            hit.source().getBody(),
                            hit.source().getPostId(),
                            hit.source().getAuthorUsername()
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed comment search query", e);
        }

        return new SearchResponseDto(
                query,
                new SearchResponseDto.ResultsDto(users, subreddits, posts, comments)
        );
    }
}
