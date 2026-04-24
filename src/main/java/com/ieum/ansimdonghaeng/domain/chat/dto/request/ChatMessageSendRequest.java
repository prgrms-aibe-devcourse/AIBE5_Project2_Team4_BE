package com.ieum.ansimdonghaeng.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(
        @NotBlank(message = "content is required")
        @Size(max = 2000, message = "content must be 2000 characters or fewer")
        String content
) {
}
