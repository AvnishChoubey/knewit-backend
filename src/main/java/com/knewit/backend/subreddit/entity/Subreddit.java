
package com.knewit.backend.subreddit.entity;

import com.knewit.backend.subreddit.enums.Topic;
import com.knewit.backend.subreddit.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subreddits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subreddit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Topic topic;
    @OneToMany(
            mappedBy = "subreddit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @Builder.Default
    private List<SubredditMember> members = new ArrayList<>();

    @OneToMany(
            mappedBy = "subreddit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SubredditModerator> moderators;


//    @OneToMany(
//            mappedBy = "subreddit",
//            cascade = CascadeType.ALL
//    )
//
//    @Builder.Default
//   private List<Post> posts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}