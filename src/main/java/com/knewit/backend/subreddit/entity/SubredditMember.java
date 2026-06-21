package com.knewit.backend.subreddit.entity;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.subreddit.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "subreddit_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "subreddit_id"}
                )
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubredditMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id", nullable = false)
    private Subreddit subreddit;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "member_state", nullable = false)
    private MemberStatus memberStatus = MemberStatus.PENDING;// PENDING, APPROVED, BANNED

    @Column(name = "is_moderator", nullable = false)
    @Builder.Default
    private Boolean isModerator = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by_user_id")
    private User bannedBy;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "ban_reason", columnDefinition = "TEXT")
    private String banReason;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;



}