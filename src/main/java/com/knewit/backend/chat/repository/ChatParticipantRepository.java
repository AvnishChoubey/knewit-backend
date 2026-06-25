package com.knewit.backend.chat.repository;
import com.knewit.backend.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findAllByUser_Id(Long userId);

    List<ChatParticipant> findAllByConversation_Id(Long conversationId);

    Optional<ChatParticipant> findByConversation_IdAndUser_Id(Long conversationId, Long userId);

    // This ensures we only fetch active conversations.
    List<ChatParticipant> findByUser_IdAndLeftAtIsNull(Long userId);

}
