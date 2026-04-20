package com.ieum.ansimdonghaeng.domain.mypage.dto.response;

import com.ieum.ansimdonghaeng.domain.user.dto.response.UserProfileResponse;
import java.math.BigDecimal;

public record MyPageSummaryResponse(
        UserProfileResponse account,
        ProjectSummary projects,
        ReviewSummary reviews,
        NotificationSummary notifications,
        FreelancerSummary freelancer
) {

    public record ProjectSummary(
            long total,
            long requested,
            long accepted,
            long inProgress,
            long completed,
            long cancelled
    ) {
    }

    public record ReviewSummary(
            long written
    ) {
    }

    public record NotificationSummary(
            long unread
    ) {
    }

    public record FreelancerSummary(
            Long freelancerProfileId,
            Boolean verified,
            Boolean publicProfile,
            BigDecimal averageRating,
            Long activityCount,
            long totalProposals,
            long pendingProposals,
            long totalVerificationRequests,
            long pendingVerificationRequests,
            long portfolioFileCount
    ) {
    }
}
