package com.ieum.ansimdonghaeng.domain.notice.dto.response;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long noticeId,
        String title,
        String content,
        LocalDateTime publishedAt
) {
}
