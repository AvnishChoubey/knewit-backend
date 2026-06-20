package com.knewit.backend.post.request;

import com.mountblue.knewit.post.enums.PostType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePostRequest {

    private String title;

    private String content;

    private String linkUrl;

    private Long subredditId;

    private PostType postType;

    private List<String> mediaUrls;
}