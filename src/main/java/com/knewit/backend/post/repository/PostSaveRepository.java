package com.knewit.backend.post.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.post.entity.PostSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostSaveRepository extends JpaRepository<PostSave, Long> {

    Optional<PostSave> findBySaver_IdAndSaved_Id(Long userId, Long postId);

    boolean existsBySaver_IdAndSaved_Id(Long userId, Long postId);

    void deleteBySaver_IdAndSaved_Id(Long userId, Long postId);

    List<PostSave> findBySaver_Id(Long userId);

    List<PostSave> findAllBySaver(User user);
}