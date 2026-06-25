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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubredditResultDto {
        private String name; // subreddit title field
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostResultDto {
        private String postId;
        private String title;
        private String authorUsername;
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
    }
}
