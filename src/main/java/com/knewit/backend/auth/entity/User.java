package com.knewit.backend.auth.entity;

import com.knewit.backend.auth.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    @Email
    private String email;

    @Column(unique = true, length = 32)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "profile_completed_at")
    private LocalDateTime profileCompletedAt;

    @Column(columnDefinition = "TEXT")
    private LocalDateTime bio;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "avatar_public_id")
    private String avatarPublicId;

//    @Column(name = "avatar_version", nullable = false)
//    @Builder.Default
//    private Long avatarVersion = 0L;

    @Column(name = "is_private_profile", nullable = false)
    @Builder.Default
    private boolean isPrivateProfile = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
