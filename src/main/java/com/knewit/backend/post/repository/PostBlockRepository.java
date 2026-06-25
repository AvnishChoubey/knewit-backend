package com.knewit.backend.post.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.post.entity.Post;
import com.knewit.backend.post.entity.PostBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostBlockRepository extends JpaRepository<PostBlock, Long> {
    void deleteByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    Optional<PostBlock> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    List<PostBlock> findAllByBlocker(User blocker);
}
