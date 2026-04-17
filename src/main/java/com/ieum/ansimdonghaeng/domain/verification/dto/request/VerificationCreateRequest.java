package com.ieum.ansimdonghaeng.domain.verification.dto.request;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationCreateRequest(
        @NotNull(message = "type is required")
        VerificationType type,

        @Size(max = 4000, message = "requestMessage must be 4000 characters or fewer")
        String requestMessage
) {
}
