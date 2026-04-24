package com.ieum.ansimdonghaeng.domain.chat.dto.request;

import jakarta.validation.constraints.Positive;

public record ChatConversationCreateRequest(
        @Positive(message = "targetUserId must be positive")
        Long targetUserId,

        @Positive(message = "targetFreelancerProfileId must be positive")
        Long targetFreelancerProfileId
) {
}
