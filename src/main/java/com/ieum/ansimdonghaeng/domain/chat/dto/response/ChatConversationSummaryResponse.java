package com.ieum.ansimdonghaeng.domain.chat.dto.response;

import com.ieum.ansimdonghaeng.domain.chat.entity.ChatConversation;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import java.time.LocalDateTime;

public record ChatConversationSummaryResponse(
        Long conversationId,
        Long otherUserId,
        String otherUserName,
        String otherUserEmail,
        String otherUserRoleCode,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {

    public static ChatConversationSummaryResponse of(ChatConversation conversation,
                                                     User otherUser,
                                                     String lastMessage,
                                                     long unreadCount) {
        return new ChatConversationSummaryResponse(
                conversation.getId(),
                otherUser.getId(),
                otherUser.getName(),
                otherUser.getEmail(),
                otherUser.getRoleCode(),
                lastMessage,
                conversation.getLastMessageAt(),
                unreadCount
        );
    }
}
