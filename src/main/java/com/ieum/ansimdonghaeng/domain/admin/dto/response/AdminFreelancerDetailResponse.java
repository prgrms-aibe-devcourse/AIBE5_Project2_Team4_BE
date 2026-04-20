package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminFreelancerDetailResponse(
        Long freelancerProfileId,
        Long userId,
        String name,
        String email,
        String roleCode,
        Boolean activeYn,
        String intro,
        String careerDescription,
        Boolean caregiverYn,
        Boolean verifiedYn,
        Boolean publicYn,
        BigDecimal averageRating,
        Long activityCount,
        List<String> activityRegionCodes,
        List<String> availableTimeSlotCodes,
        List<String> projectTypeCodes,
        List<RecentVerificationResponse> recentVerifications,
        List<PortfolioFileResponse> portfolioFiles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record RecentVerificationResponse(
            Long verificationId,
            VerificationType verificationType,
            VerificationStatus status,
            LocalDateTime requestedAt,
            LocalDateTime reviewedAt,
            String rejectReason
    ) {
    }

    public record PortfolioFileResponse(
            Long verificationFileId,
            String originalName,
            String contentType,
            Long fileSize,
            String viewUrl,
            String downloadUrl,
            LocalDateTime uploadedAt
    ) {
    }
}
