package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthReissueRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
