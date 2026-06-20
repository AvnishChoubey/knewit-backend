package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByChatRoomId(Long chatRoomId);

    @Query("SELECT m.userId FROM ChatRoomMember m WHERE m.chatRoomId = :roomId")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);
}