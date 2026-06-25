package com.knewit.backend.search.repository;

import com.knewit.backend.search.entity.SearchIndexSyncEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchIndexSyncEventRepository extends JpaRepository<SearchIndexSyncEvent, UUID> {

    @Query("SELECT e FROM SearchIndexSyncEvent e WHERE e.status = 'PENDING' AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now) ORDER BY e.createdAt ASC")
    List<SearchIndexSyncEvent> findPendingEvents(@Param("now") Instant now);

    List<SearchIndexSyncEvent> findTop100ByStatusAndNextAttemptAtLessThanEqual(String status, LocalDateTime time);
}
