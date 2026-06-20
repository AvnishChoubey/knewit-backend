package com.knewit.backend.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter @Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type", nullable = false)
    private String roomType; // "DIRECT" or "GROUP"

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "last_message_preview")
    private String lastMessagePreview;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<ChatRoomMember> members;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }
}