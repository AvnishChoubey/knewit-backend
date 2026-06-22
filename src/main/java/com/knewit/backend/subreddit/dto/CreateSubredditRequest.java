package com.knewit.backend.subreddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubredditRequest {

    @NotBlank(message = "Subreddit name is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{3,80}$",
            message = "Name must be alphanumeric and between 3 to 80 characters"
    )
    private String name;

    @NotBlank(message = "Subreddit title is required")
    private String title;

    private String description;

    private String topic;

    private String visibility;

    private String postingPolicy;

    private String iconUrl;

    private String iconPublicId;
}