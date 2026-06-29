package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
            @Param("keyword") String keyword,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    Optional<Post> findByIdAndPostStatus(
            Long id,
            PostStatus postStatus
    );

    Page<Post> findBySubreddit_IdAndPostStatus(
            Long subredditId,
            PostStatus postStatus,
            Pageable pageable
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

    @Query("SELECT COALESCE(SUM(COALESCE(p.upvoteCount, 0) - COALESCE(p.downvoteCount, 0)), 0) FROM Post p WHERE p.author.id = :authorId AND p.postStatus = com.knewit.backend.post.enums.PostStatus.PUBLISHED")
    long sumPostScoreByAuthorId(@Param("authorId") Long authorId);

    long countByAuthor_IdAndPostStatus(Long authorId, PostStatus postStatus);

    Page<Post> findBySubreddit_IdInAndPostStatus(
            List<Long> subredditIds,
            PostStatus postStatus,
            Pageable pageable
    );

    Page<Post> findByPostStatusAndIdLessThan(
            PostStatus postStatus,
            Long id,
            Pageable pageable
    );

    Page<Post> findBySubreddit_IdInAndPostStatusAndIdLessThan(
            List<Long> subredditIds,
            PostStatus postStatus,
            Long id,
            Pageable pageable
    );
}