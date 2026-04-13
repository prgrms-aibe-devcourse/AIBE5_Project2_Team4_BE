package com.ieum.ansimdonghaeng.domain.auth.dto.response;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {
}
