package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import java.time.LocalDateTime;

public record AdminNoticeResponse(
        Long noticeId,
        String title,
        String content,
        Boolean publishedYn,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AdminUserSummaryResponse admin
) {
}
