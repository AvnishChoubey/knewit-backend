package com.knewit.backend.user.repository;

import com.knewit.backend.user.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    List<UserBlock> findAllByBlockerId(Long blockerId);
}
