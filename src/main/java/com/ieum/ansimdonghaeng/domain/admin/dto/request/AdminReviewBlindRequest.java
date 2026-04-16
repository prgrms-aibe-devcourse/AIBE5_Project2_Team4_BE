package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminReviewBlindRequest(
        @NotBlank(message = "reason is required.")
        String reason
) {
}
