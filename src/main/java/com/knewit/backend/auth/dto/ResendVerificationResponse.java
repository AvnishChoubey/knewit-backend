package com.knewit.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResendVerificationResponse {
    private boolean sent;
    private String verificationTokenExpiresAt;
}