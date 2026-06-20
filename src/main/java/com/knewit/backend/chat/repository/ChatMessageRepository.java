package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.senderUsername = :user1 AND m.receiverUsername = :user2) OR " +
            "(m.senderUsername = :user2 AND m.receiverUsername = :user1) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("user1") String user1,
                                       @Param("user2") String user2);

    @Query("SELECT DISTINCT CASE WHEN m.senderUsername = :user " +
            "THEN m.receiverUsername ELSE m.senderUsername END " +
            "FROM ChatMessage m WHERE m.senderUsername = :user OR m.receiverUsername = :user")
    List<String> findConversationPartners(@Param("user") String user);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoomId = :roomId ORDER BY m.createdAt ASC")
    List<ChatMessage> findByRoomId(@Param("roomId") Long roomId);
}