package com.knewit.backend.post.entity;

import com.knewit.backend.post.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType; // IMAGE, VIDEO, URL, TEXT

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_url", nullable = false, columnDefinition = "TEXT")
    private String cloudinaryUrl;

    @Column(name = "byte_size", nullable = false)
    private Long byteSize;

//    @Column(name = "duration_seconds")
//    private Integer durationSeconds;

 //   private Integer width;
//    private Integer height;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
