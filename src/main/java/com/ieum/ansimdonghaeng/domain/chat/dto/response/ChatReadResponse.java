package com.ieum.ansimdonghaeng.domain.chat.dto.response;

public record ChatReadResponse(
        Long conversationId,
        int readCount
) {
}
