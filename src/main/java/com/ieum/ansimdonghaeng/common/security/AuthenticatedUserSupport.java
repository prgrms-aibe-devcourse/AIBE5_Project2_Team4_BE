package com.ieum.ansimdonghaeng.common.security;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;

public final class AuthenticatedUserSupport {

    private AuthenticatedUserSupport() {
    }

    public static Long currentUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Authenticated user id is required.");
        }
        return userDetails.getUserId();
    }
}
