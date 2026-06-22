package com.knewit.backend.subreddit.entity;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.subreddit.enums.PostingPolicy;
import com.knewit.backend.subreddit.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subreddits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subreddit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name; // The slug name used in URL (e.g. "programming")

    @Column(nullable = false, length = 120)
    private String title; // Human-readable title (e.g. "Programming Community")

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "posting_policy", nullable = false)
    private PostingPolicy postingPolicy = PostingPolicy.OPEN;// OPEN, RESTRICTED

    @Column(name = "icon_url", columnDefinition = "TEXT")
    private String iconUrl;

    @Column(name = "icon_public_id")
    private String iconPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Topic topic = Topic.OTHER;

    @Column(name = "member_count", nullable = false)
    @Builder.Default
    private Long memberCount = 0L;

    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Long postCount = 0L;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;


    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
