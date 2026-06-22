package com.knewit.backend.post.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.entity.PostSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostSaveRepository extends JpaRepository<PostSave, Long> {

    Optional<PostSave> findBySaverIdAndSavedId(Long userId, Long postId);

    boolean existsBySaverIdAndSavedId(Long userId, Long postId);

    void deleteBySaverIdAndSavedId(Long userId, Long postId);

    List<PostSave> findBySaverId(Long userId);

    List<PostSave> findAllBySaver(User user);

    Optional<PostSave> findBySaverAndSaved(User saver, Post saved);
}