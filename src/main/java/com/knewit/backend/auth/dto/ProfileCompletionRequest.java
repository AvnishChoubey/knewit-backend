package com.knewit.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCompletionRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotEmpty(message = "At least one interest is required")
    private List<String> interests;

    private String bio;
    private MultipartFile avatar;
}
