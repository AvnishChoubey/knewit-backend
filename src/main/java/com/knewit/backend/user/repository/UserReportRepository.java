package com.knewit.backend.user.repository;

import com.knewit.backend.user.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    Optional<UserReport> findByReporterIdAndReportedIdAndReason(Long reporterId, Long reportedId, String reason);
}
