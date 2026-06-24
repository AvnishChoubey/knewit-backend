package com.knewit.backend.auth.repository;

import com.knewit.backend.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String tokenHash);
    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
