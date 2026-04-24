package com.ieum.ansimdonghaeng.domain.chat.dto.response;

import com.ieum.ansimdonghaeng.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long conversationId,
        Long senderUserId,
        String senderName,
        String senderRoleCode,
        String content,
        Boolean readYn,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getSender().getRoleCode(),
                message.getContent(),
                message.getReadYn(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }
}
