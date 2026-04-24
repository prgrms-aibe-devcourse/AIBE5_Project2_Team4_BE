package com.ieum.ansimdonghaeng.domain.chat.repository;

import com.ieum.ansimdonghaeng.domain.chat.entity.ChatConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByParticipantA_IdAndParticipantB_Id(Long participantAUserId, Long participantBUserId);

    @Query("""
            select c
            from ChatConversation c
            join fetch c.participantA
            join fetch c.participantB
            where c.participantA.id = :userId or c.participantB.id = :userId
            order by c.lastMessageAt desc, c.id desc
            """)
    List<ChatConversation> findAllByParticipantUserId(@Param("userId") Long userId);

    @Query("""
            select c
            from ChatConversation c
            join fetch c.participantA
            join fetch c.participantB
            where c.id = :conversationId
            """)
    Optional<ChatConversation> findDetailById(@Param("conversationId") Long conversationId);
}
