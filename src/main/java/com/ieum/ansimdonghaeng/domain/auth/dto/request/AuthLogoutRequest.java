package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthLogoutRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken
) {
}
