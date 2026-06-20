package com.knewit.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {
    private String query;
    private ResultsDto results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultsDto {
        private List<UserResultDto> users;
        private List<SubredditResultDto> subreddits;
        private List<PostResultDto> posts;
        private List<CommentResultDto> comments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResultDto {
        private String username;
        private String displayName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubredditResultDto {
        private String slug; // subreddit name field
        private String name; // subreddit title field
        private String iconUrl;
        private String visibility;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostResultDto {
        private String postId;
        private String title;
        private String subredditSlug;
        private String authorUsername;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResultDto {
        private String commentId;
        private String body;
        private String postId;
        private String authorUsername;
        private String createdAt;
    }
}
