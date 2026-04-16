package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import java.time.LocalDateTime;

public record AdminReportDetailResponse(
        Long reportId,
        ReportReasonType reasonType,
        String reasonDetail,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime handledAt,
        AdminUserSummaryResponse reporter,
        AdminUserSummaryResponse handledBy,
        ReviewSummaryResponse review
) {

    public record ReviewSummaryResponse(
            Long reviewId,
            Long projectId,
            String projectTitle,
            Integer rating,
            Boolean blindedYn,
            AdminUserSummaryResponse writer,
            AdminFreelancerSummaryResponse targetFreelancer,
            LocalDateTime createdAt
    ) {
    }
}
