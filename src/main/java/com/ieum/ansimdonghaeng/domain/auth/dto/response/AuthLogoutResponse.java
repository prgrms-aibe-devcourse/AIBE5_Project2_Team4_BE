package com.ieum.ansimdonghaeng.domain.auth.dto.response;

public record AuthLogoutResponse(
        long revokedRefreshTokenCount
) {
}
