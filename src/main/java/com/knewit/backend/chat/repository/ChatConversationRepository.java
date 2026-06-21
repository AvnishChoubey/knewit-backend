package com.knewit.backend.chat.repository;

import com.knewit.backend.chat.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
}
