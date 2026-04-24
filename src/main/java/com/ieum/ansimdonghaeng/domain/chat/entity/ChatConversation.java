package com.ieum.ansimdonghaeng.domain.chat.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_CONVERSATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "chat_conversation_seq_generator", sequenceName = "SEQ_CHAT_CONVERSATION", allocationSize = 1)
public class ChatConversation extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_conversation_seq_generator")
    @Column(name = "CHAT_CONVERSATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PARTICIPANT_A_USER_ID", nullable = false)
    private User participantA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PARTICIPANT_B_USER_ID", nullable = false)
    private User participantB;

    @Column(name = "LAST_MESSAGE_AT")
    private LocalDateTime lastMessageAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatConversation(User participantA, User participantB, LocalDateTime lastMessageAt) {
        this.participantA = participantA;
        this.participantB = participantB;
        this.lastMessageAt = lastMessageAt;
    }

    public static ChatConversation create(User participantA, User participantB) {
        if (participantA.getId() > participantB.getId()) {
            return new ChatConversation(participantB, participantA, null);
        }
        return new ChatConversation(participantA, participantB, null);
    }

    public boolean involves(Long userId) {
        return Objects.equals(participantA.getId(), userId) || Objects.equals(participantB.getId(), userId);
    }

    public User otherParticipant(Long userId) {
        if (Objects.equals(participantA.getId(), userId)) {
            return participantB;
        }
        if (Objects.equals(participantB.getId(), userId)) {
            return participantA;
        }
        throw new IllegalArgumentException("User is not a participant in this conversation.");
    }

    public void touch(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
