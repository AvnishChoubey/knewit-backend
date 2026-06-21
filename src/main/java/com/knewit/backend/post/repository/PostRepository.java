package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
       SELECT p
       FROM Post p
       WHERE p.postStatus = :status
       AND (
            LOWER(p.title)
            LIKE LOWER(CONCAT('%', :keyword, '%'))

            OR

            LOWER(COALESCE(p.body, ''))
            LIKE LOWER(CONCAT('%', :keyword, '%'))

            OR

            LOWER(p.subreddit.name)
            LIKE LOWER(CONCAT('%', :keyword, '%'))
       )
       """)
    Page<Post> searchPosts(
            String keyword,
            PostStatus status,
            Pageable pageable
    );

    Optional<Post> findByIdAndPostStatus(
            Long id,
            PostStatus postStatus
    );

    Page<Post> findBySubreddit_NameAndPostStatus(
            String subredditName,
            PostStatus postStatus,
            Pageable pageable
    );

    Page<Post> findByPostStatus(
            PostStatus postStatus,
            Pageable pageable
    );

    boolean existsByIdAndAuthor_Id(
            Long postId,
            Long authorId
    );

    Optional<Post> findByIdAndAuthor_Id(
            Long postId,
            Long authorId
    );

    Page<Post> findByAuthor_IdAndPostStatus(
            Long authorId,
            PostStatus postStatus,
            Pageable pageable
    );

}