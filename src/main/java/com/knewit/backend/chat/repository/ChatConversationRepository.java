package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatConversation;
import com.knewit.backend.chat.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    List<ChatConversation> findByConversationType(ConversationType conversationType);
}
