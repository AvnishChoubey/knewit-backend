package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Find all rooms a user is a member of
    @Query("SELECT cr FROM ChatRoom cr JOIN ChatRoomMember m ON cr.id = m.chatRoomId " +
            "WHERE m.userId = :userId ORDER BY cr.lastActivityAt DESC")
    List<ChatRoom> findRoomsByUserId(@Param("userId") Long userId);

    // Find existing DIRECT room between two users
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'DIRECT' AND " +
            "EXISTS (SELECT m FROM ChatRoomMember m WHERE m.chatRoomId = cr.id AND m.userId = :user1) AND " +
            "EXISTS (SELECT m FROM ChatRoomMember m WHERE m.chatRoomId = cr.id AND m.userId = :user2)")
    Optional<ChatRoom> findDirectRoom(@Param("user1") Long user1, @Param("user2") Long user2);
}