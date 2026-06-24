package com.knewit.backend.user.repository;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.user.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
    void deleteByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
    List<UserBlock> findAllByBlocker_Id(Long blockerId);
    Optional<UserBlock> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
}
