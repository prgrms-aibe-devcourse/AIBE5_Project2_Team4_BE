package com.ieum.ansimdonghaeng.domain.chat.repository;

import com.ieum.ansimdonghaeng.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = "sender")
    Page<ChatMessage> findAllByConversation_IdOrderByCreatedAtDescIdDesc(Long conversationId, Pageable pageable);

    @EntityGraph(attributePaths = "sender")
    ChatMessage findTop1ByConversation_IdOrderByCreatedAtDescIdDesc(Long conversationId);

    long countByConversation_IdAndSender_IdNotAndReadYnFalse(Long conversationId, Long currentUserId);

    List<ChatMessage> findAllByConversation_IdInAndSender_IdNotAndReadYnFalse(List<Long> conversationIds, Long currentUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage m
            set m.readYn = true, m.readAt = :readAt
            where m.conversation.id = :conversationId
              and m.sender.id <> :currentUserId
              and m.readYn = false
            """)
    int markConversationAsRead(@Param("conversationId") Long conversationId,
                               @Param("currentUserId") Long currentUserId,
                               @Param("readAt") LocalDateTime readAt);
}
