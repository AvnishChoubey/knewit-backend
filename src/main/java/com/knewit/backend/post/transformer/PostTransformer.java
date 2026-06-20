package com.knewit.backend.post.transformer;

import com.mountblue.knewit.post.entity.Media;
import com.mountblue.knewit.post.entity.Post;
import com.mountblue.knewit.post.response.PostResponse;

import java.util.List;

public class PostTransformer {

    private PostTransformer() {
    }

    public static PostResponse toResponse(Post post) {

        List<String> mediaUrls = post.getMedia()
                .stream()
                .map(Media::getMediaUrl)
                .toList();

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .linkUrl(post.getLinkUrl())
                .postType(post.getPostType())
                .author(post.getAuthor().getUsername())
                .subredditId(post.getSubreddit().getId())
                .subredditName(post.getSubreddit().getName())
                .mediaUrls(mediaUrls)
                .edited(post.getEdited())
                .createdAt(post.getCreatedAt())
                .build();
    }
}