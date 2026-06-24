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
public class UpdateMyProfileRequest {
    private String bio;
    private List<Topic> interests;
    private String avatarUrl;
    private String avatarPublicId;
}
