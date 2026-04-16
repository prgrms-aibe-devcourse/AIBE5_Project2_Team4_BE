package com.ieum.ansimdonghaeng.domain.admin.dto.response;

public record AdminUserSummaryResponse(
        Long userId,
        String name,
        String email,
        String roleCode,
        Boolean activeYn
) {
}
