package com.knewit.backend.comment.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.comment.entity.CommentSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentSaveRepository extends JpaRepository<CommentSave, Long> {

    Optional<CommentSave> findBySaverIdAndSavedId(Long userId, Long commentId);

    boolean existsBySaverIdAndSavedId(Long userId, Long commentId);

    List<CommentSave> findAllBySaver(User user);

    void deleteBySaverIdAndSavedId(Long userId, Long commentId);
}