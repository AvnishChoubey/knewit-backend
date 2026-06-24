package com.knewit.backend.comment.entity;

import com.knewit.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_blockers",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "comment_id",
                                "user_id"
                        }
                )
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment blocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User blocker;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
