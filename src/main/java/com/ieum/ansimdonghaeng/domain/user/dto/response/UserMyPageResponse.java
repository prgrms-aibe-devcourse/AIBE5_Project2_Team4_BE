package com.ieum.ansimdonghaeng.domain.user.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserMyPageResponse(
        UserSummary user,
        ProjectStats projectStats,
        ReviewStats reviewStats,
        NotificationSummary notificationSummary,
        FreelancerProfileSummary freelancerProfile,
        VerificationSummary verificationSummary,
        ProposalSummary proposalSummary
) {

    public record UserSummary(
            Long userId,
            String email,
            String name,
            String phone,
            String intro,
            String roleCode,
            Boolean activeYn
    ) {
    }

    public record ProjectStats(
            long totalProjects,
            long requestedProjects,
            long acceptedProjects,
            long inProgressProjects,
            long completedProjects,
            long cancelledProjects
    ) {
    }

    public record ReviewStats(
            long writtenReviewCount,
            long reportedCount
    ) {
    }

    public record NotificationSummary(
            long unreadNotificationCount
    ) {
    }

    public record FreelancerProfileSummary(
            Long freelancerProfileId,
            Boolean verifiedYn,
            Boolean publicYn,
            Boolean caregiverYn,
            BigDecimal averageRating,
            Long activityCount
    ) {
    }

    public record VerificationSummary(
            Long verificationId,
            VerificationType type,
            VerificationStatus status,
            LocalDateTime requestedAt,
            LocalDateTime reviewedAt,
            String rejectReason
    ) {
    }

    public record ProposalSummary(
            long receivedProposalCount,
            long pendingReceivedProposalCount
    ) {
    }
}
