package com.knewit.backend.post.entity;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.post.enums.PostStatus;
import com.knewit.backend.post.enums.PostType;
import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id", nullable = false)
    private Subreddit subreddit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type; // TEXT, IMAGE, VIDEO, URL

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false)
    @Builder.Default
    private PostStatus postStatus = PostStatus.PUBLISHED;// DRAFT, PENDING_APPROVAL, PUBLISHED, REMOVED, ARCHIVED


    @Column(name = "upvote_count", nullable = false)
    @Builder.Default
    private Long upvoteCount = 0L;

    @Column(name = "downvote_count", nullable = false)
    @Builder.Default
    private Long downvoteCount = 0L;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private Long shareCount = 0L;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "report_count", nullable = false)
    @Builder.Default
    private Long reportCount = 0L;

    @Column(name = "score_hot", nullable = false)
    @Builder.Default
    private Double scoreHot = 0.0;

    @Column(name = "score_best", nullable = false)
    @Builder.Default
    private Double scoreBest = 0.0;

    @Column(name = "score_rising", nullable = false)
    @Builder.Default
    private Double scoreRising = 0.0;

    @Column(name = "score_top", nullable = false)
    @Builder.Default
    private Double scoreTop = 0.0;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "removed_by_user_id")
    private User removedBy;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "removed_reason", columnDefinition = "TEXT")
    private String removedReason;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
