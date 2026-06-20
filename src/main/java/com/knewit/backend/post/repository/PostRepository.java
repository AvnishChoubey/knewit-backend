package com.knewit.backend.post.repository;

import com.mountblue.knewit.post.entity.Post;
import com.mountblue.knewit.post.entity.Subreddit;
import com.mountblue.knewit.post.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository
        extends JpaRepository<Post, Long> {

    List<Post> findBySubreddit(Subreddit subreddit);

    List<Post> findByAuthor(User author);

}