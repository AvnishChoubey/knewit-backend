package com.knewit.backend.post.response;

import com.mountblue.knewit.post.enums.PostType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PostResponse {

    private Long id;

    private String title;

    private String content;

    private String linkUrl;

    private PostType postType;

    private String author;

    private Long subredditId;

    private String subredditName;

    private List<String> mediaUrls;

    private Boolean edited;

    private LocalDateTime createdAt;
}