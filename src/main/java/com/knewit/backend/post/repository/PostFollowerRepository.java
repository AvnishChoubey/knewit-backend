package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.PostFollower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostFollowerRepository
        extends JpaRepository<PostFollower, Long> {

    Optional<PostFollower>
    findByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    boolean existsByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    List<PostFollower>
    findByUser_Id(
            Long userId
    );
}