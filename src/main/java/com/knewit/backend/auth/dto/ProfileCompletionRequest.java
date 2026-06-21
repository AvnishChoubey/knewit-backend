package com.knewit.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ProfileCompletionRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotEmpty(message = "At least one interest is required")
    private List<String> interests;

    private String bio;
    private String avatarUrl;
    private String avatarPublicId;
}
