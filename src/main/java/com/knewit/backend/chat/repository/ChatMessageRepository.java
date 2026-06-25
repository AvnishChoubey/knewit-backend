package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversation_IdAndDeletedAtIsNullOrderBySentAtDesc(Long conversationId,
                                                                              Pageable pageable);
    Optional<ChatMessage> findByIdAndConversation_Id(Long messageId, Long conversationId);
}
