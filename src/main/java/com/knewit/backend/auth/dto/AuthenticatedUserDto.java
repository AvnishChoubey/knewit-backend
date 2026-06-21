package com.knewit.backend.auth.dto;

import com.knewit.backend.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedUserDto {
    private Long userId;
    private String email;
    private String username;
    private Role role;
    private boolean profileCompleted;
    private boolean verified;
}
