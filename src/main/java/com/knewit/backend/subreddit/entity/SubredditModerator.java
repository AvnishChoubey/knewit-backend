package com.knewit.backend.subreddit.entity;

import com.knewit.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "subreddit_moderators",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "subreddit_id"}
                )
        }
)
public class SubredditModerator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subreddit_id")
    private Subreddit subreddit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeratorRole role;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    public void prePersist() {
        assignedAt = LocalDateTime.now();
    }

    public enum ModeratorRole {
        OWNER,
        MODERATOR
    }
}