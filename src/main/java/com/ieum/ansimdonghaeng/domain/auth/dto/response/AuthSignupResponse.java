package com.ieum.ansimdonghaeng.domain.auth.dto.response;

import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;

public record AuthSignupResponse(
        String username,
        UserRole role,
        String message
) {
}
