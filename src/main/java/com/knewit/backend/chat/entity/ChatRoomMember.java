package com.knewit.backend.chat.entity;

import com.knewit.backend.subreddit.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
@Getter @Setter
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", insertable = false, updatable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}