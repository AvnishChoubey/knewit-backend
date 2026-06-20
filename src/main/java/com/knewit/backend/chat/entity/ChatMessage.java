package com.knewit.backend.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_username")
    private String senderUsername;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_username")
    private String receiverUsername;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "chat_room_id")
    private Long chatRoomId;   // bigint in DB so must be Long, not String

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.sentAt == null)   this.sentAt   = now;
        if (this.createdAt == null) this.createdAt = now;  // already there, just keep it
    }
}