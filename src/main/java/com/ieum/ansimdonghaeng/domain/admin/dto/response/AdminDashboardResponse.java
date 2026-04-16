package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResponse(
        long totalUsers,
        long totalFreelancers,
        long verifiedFreelancers,
        long pendingVerifications,
        long requestedProjects,
        long acceptedProjects,
        long inProgressProjects,
        long completedProjects,
        long cancelledProjects,
        long pendingReports,
        long blindedReviews,
        long publishedNotices,
        List<RecentVerification> recentPendingVerifications,
        List<RecentReport> recentReports,
        List<RecentProject> recentProjects
) {

    public record RecentVerification(
            Long verificationId,
            Long freelancerProfileId,
            String applicantName,
            String verificationType,
            LocalDateTime requestedAt
    ) {
    }

    public record RecentReport(
            Long reportId,
            Long reviewId,
            String reporterName,
            ReportReasonType reasonType,
            LocalDateTime createdAt
    ) {
    }

    public record RecentProject(
            Long projectId,
            String title,
            ProjectStatus status,
            String ownerName,
            LocalDateTime createdAt
    ) {
    }
}
