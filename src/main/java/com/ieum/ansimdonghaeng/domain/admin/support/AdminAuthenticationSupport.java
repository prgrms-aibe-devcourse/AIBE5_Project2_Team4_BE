package com.ieum.ansimdonghaeng.domain.admin.support;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;

public final class AdminAuthenticationSupport {

    private AdminAuthenticationSupport() {
    }

    public static Long currentUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Authenticated user id is required.");
        }
        return userDetails.getUserId();
    }
}
