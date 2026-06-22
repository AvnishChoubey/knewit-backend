package com.knewit.backend.subreddit.dto;

import com.knewit.backend.subreddit.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubredditPublicViewResponse {

    private SubredditDto subreddit;

    private ViewerState viewerState;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewerState {

        private boolean isMember;

        private boolean isModerator;

        private boolean isCreator;

        private MemberStatus membershipStatus;

        private MemberStatus postingStatus;
    }
}