package com.ieum.ansimdonghaeng.domain.notice.dto.response;

import java.time.LocalDateTime;

public record NoticeSummaryResponse(
        Long noticeId,
        String title,
        LocalDateTime publishedAt,
        LocalDateTime createdAt
) {
}
