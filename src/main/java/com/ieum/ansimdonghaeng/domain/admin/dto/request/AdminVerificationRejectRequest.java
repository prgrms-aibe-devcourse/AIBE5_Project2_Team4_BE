package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminVerificationRejectRequest(
        @NotBlank(message = "reason is required.")
        String reason
) {
}
