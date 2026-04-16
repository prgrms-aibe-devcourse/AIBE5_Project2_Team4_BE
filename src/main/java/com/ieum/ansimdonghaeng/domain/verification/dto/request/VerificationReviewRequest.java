package com.ieum.ansimdonghaeng.domain.verification.dto.request;

import jakarta.validation.constraints.Size;

public record VerificationReviewRequest(
        @Size(max = 1000, message = "reviewComment must be 1000 characters or fewer")
        String reviewComment
) {
}
