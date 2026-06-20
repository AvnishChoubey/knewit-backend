
package com.knewit.backend.subreddit.transformer;

import com.knewit.backend.subreddit.response.SubredditResponse;

public class SubredditTransformer {

    public static SubredditResponse toResponse(
            Subreddit subreddit,
            Long memberCount
    ) {

        return SubredditResponse.builder()
                .id(subreddit.getId())
                .name(subreddit.getName())
                .description(subreddit.getDescription())
                .bannerUrl(subreddit.getBannerUrl())
                .iconUrl(subreddit.getIconUrl())
                .visibility(subreddit.getVisibility())
                .topic(subreddit.getTopic())
                .createdBy(subreddit.getCreatedBy().getUsername())
                .memberCount(memberCount)
                .build();
    }
}