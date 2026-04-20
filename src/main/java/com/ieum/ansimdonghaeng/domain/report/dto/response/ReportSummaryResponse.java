package com.ieum.ansimdonghaeng.domain.report.dto.response;

import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import java.time.LocalDateTime;

public record ReportSummaryResponse(
        Long reportId,
        Long reviewId,
        ReportReasonType reasonType,
        String reasonDetailSummary,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime handledAt,
        HandledBySummary handledBy,
        ReviewSummary review
) {

    public static ReportSummaryResponse from(Report report) {
        return new ReportSummaryResponse(
                report.getId(),
                report.getReview().getId(),
                report.getReasonType(),
                summarize(report.getReasonDetail()),
                report.getStatus(),
                report.getCreatedAt(),
                report.getHandledAt(),
                report.getHandledByUser() == null
                        ? null
                        : new HandledBySummary(report.getHandledByUser().getId(), report.getHandledByUser().getName()),
                new ReviewSummary(
                        report.getReview().getProject().getId(),
                        report.getReview().getProject().getTitle(),
                        report.getReview().getRating(),
                        summarize(report.getReview().getContent())
                )
        );
    }

    private static String summarize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120);
    }

    public record HandledBySummary(
            Long userId,
            String name
    ) {
    }

    public record ReviewSummary(
            Long projectId,
            String projectTitle,
            Integer rating,
            String contentSummary
    ) {
    }
}
