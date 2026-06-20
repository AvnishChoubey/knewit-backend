package com.knewit.backend.post.service;

import com.mountblue.knewit.post.entity.Media;
import com.mountblue.knewit.post.entity.Post;
import com.mountblue.knewit.post.entity.Subreddit;
import com.mountblue.knewit.post.entity.User;
import com.mountblue.knewit.post.enums.MediaType;
import com.mountblue.knewit.post.repository.MediaRepository;
import com.mountblue.knewit.post.repository.PostRepository;
import com.mountblue.knewit.post.repository.SubredditRepository;
import com.mountblue.knewit.post.repository.UserRepository;
import com.mountblue.knewit.post.request.CreatePostRequest;
import com.mountblue.knewit.post.response.PostResponse;
import com.mountblue.knewit.post.transformer.PostTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final MediaRepository mediaRepository;

    public PostResponse createPost(
            Long userId,
            CreatePostRequest request
    ) {

        User author = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subreddit subreddit = subredditRepository
                .findById(request.getSubredditId())
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setLinkUrl(request.getLinkUrl());
        post.setPostType(request.getPostType());

        post.setAuthor(author);
        post.setSubreddit(subreddit);

        post = postRepository.save(post);

        if (request.getMediaUrls() != null
                && !request.getMediaUrls().isEmpty()) {

            for (String mediaUrl : request.getMediaUrls()) {

                Media media = new Media();

                media.setMediaUrl(mediaUrl);

                media.setMediaType(MediaType.IMAGE);

                media.setPost(post);

                media = mediaRepository.save(media);

                post.getMedia().add(media);
            }
        }

        return PostTransformer.toResponse(post);
    }

    public PostResponse getPostById(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        return PostTransformer.toResponse(post);
    }

    public List<PostResponse> getAllPosts() {

        return postRepository.findAll()
                .stream()
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .map(PostTransformer::toResponse)
                .toList();
    }

    public List<PostResponse> getPostsBySubreddit(
            Long subredditId
    ) {

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        return postRepository.findBySubreddit(subreddit)
                .stream()
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .map(PostTransformer::toResponse)
                .toList();
    }

    public PostResponse updatePost(
            Long postId,
            CreatePostRequest request
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setLinkUrl(request.getLinkUrl());

        post.setEdited(true);

        post = postRepository.save(post);

        return PostTransformer.toResponse(post);
    }

    public void deletePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new RuntimeException("Post not found"));

        post.setDeleted(true);

        postRepository.save(post);
    }
}