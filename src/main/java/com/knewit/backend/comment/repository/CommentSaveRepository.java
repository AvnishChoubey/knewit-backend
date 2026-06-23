package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.CommentSave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentSaveRepository
        extends JpaRepository<CommentSave, Long> {

    Optional<CommentSave>
    findByComment_IdAndUser_Id(
            Long commentId,
            Long userId
    );

    boolean existsByComment_IdAndUser_Id(
            Long commentId,
            Long userId
    );
}