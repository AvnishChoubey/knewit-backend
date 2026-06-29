package com.knewit.backend.search.repository;

import com.knewit.backend.search.entity.SearchIndexSyncEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchIndexSyncEventRepository extends JpaRepository<SearchIndexSyncEvent, UUID> {

    @Query("SELECT e FROM SearchIndexSyncEvent e WHERE e.status = :status AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :time) ORDER BY e.createdAt ASC")
    List<SearchIndexSyncEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrNull(@Param("status") String status, @Param("time") LocalDateTime time, Pageable pageable);
}