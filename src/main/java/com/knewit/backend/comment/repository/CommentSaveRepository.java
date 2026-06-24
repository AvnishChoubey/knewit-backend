package com.knewit.backend.comment.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.comment.entity.CommentSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentSaveRepository extends JpaRepository<CommentSave, Long> {

    Optional<CommentSave> findBySaver_IdAndSaved_Id(Long userId, Long commentId);

    boolean existsBySaver_IdAndSaved_Id(Long userId, Long commentId);

    List<CommentSave> findAllBySaver(User user);

    void deleteBySaver_IdAndSaved_Id(Long userId, Long commentId);
}