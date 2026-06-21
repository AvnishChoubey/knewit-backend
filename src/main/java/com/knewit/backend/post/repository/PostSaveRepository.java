package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.PostSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostSaveRepository
        extends JpaRepository<PostSave, Long> {

    Optional<PostSave> findByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    boolean existsByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    void deleteByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    List<PostSave> findByUser_Id(Long userId);
}