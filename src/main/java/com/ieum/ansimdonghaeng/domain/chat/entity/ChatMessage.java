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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "chat_message_seq_generator", sequenceName = "SEQ_CHAT_MESSAGE", allocationSize = 1)
public class ChatMessage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_message_seq_generator")
    @Column(name = "CHAT_MESSAGE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CHAT_CONVERSATION_ID", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SENDER_USER_ID", nullable = false)
    private User sender;

    @Column(name = "CONTENT", nullable = false, length = 2000)
    private String content;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "READ_YN", nullable = false, length = 1)
    private Boolean readYn;

    @Column(name = "READ_AT")
    private LocalDateTime readAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ChatMessage(ChatConversation conversation, User sender, String content, Boolean readYn, LocalDateTime readAt) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.readYn = readYn;
        this.readAt = readAt;
    }

    public static ChatMessage create(ChatConversation conversation, User sender, String content) {
        return new ChatMessage(conversation, sender, content, false, null);
    }

    public void markRead(LocalDateTime readAt) {
        if (Boolean.TRUE.equals(readYn)) {
            return;
        }
        this.readYn = true;
        this.readAt = readAt;
    }
}
