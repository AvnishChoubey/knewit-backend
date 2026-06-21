package com.knewit.backend.comment.entity;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.comment.enums.CommentStatus;
import com.knewit.backend.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "depth_level", nullable = false)
    @Builder.Default
    private Integer depthLevel = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "content_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommentStatus commentStatus = CommentStatus.PUBLISHED; // DRAFT, PUBLISHED, REMOVED, ARCHIVED

    @Column(name = "upvote_count", nullable = false)
    @Builder.Default
    private Long upvoteCount = 0L;

    @Column(name = "downvote_count", nullable = false)
    @Builder.Default
    private Long downvoteCount = 0L;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private Long shareCount = 0L;

    @Column(name = "report_count", nullable = false)
    @Builder.Default
    private Long reportCount = 0L;

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
