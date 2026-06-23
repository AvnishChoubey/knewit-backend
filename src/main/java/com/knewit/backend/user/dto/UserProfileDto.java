package com.knewit.backend.user.dto;

import com.knewit.backend.common.enums.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private long id;
    private String username;
    private String bio;
    private String avatarUrl;
    private String avatarPublicId;
    private List<Topic> interests;
    private long followersCount;
    private long followingCount;
    private String createdAt;
    private String updatedAt;
}
