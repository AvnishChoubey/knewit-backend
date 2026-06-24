package com.knewit.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSignUpResponse {
    private Long userId;
    private String email;
    @Builder.Default
    private boolean verificationRequired = true;
    private String verificationTokenExpiresAt;
}
