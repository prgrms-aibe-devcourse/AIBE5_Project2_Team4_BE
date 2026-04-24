package com.ieum.ansimdonghaeng.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatConversationCreateRequest(
        @NotNull(message = "targetUserId is required")
        Long targetUserId
) {
}
