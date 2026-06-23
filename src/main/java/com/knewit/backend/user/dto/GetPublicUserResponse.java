package com.knewit.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetPublicUserResponse {
    private UserProfileDto profile;
    private boolean isFollowing;
    private boolean isBlocked;
    private boolean isBlockedByViewer;
}
