package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import java.time.LocalDateTime;

public record AdminReportListItemResponse(
        Long reportId,
        Long reviewId,
        AdminUserSummaryResponse reporter,
        ReportReasonType reasonType,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime handledAt,
        AdminUserSummaryResponse handledBy
) {
}
