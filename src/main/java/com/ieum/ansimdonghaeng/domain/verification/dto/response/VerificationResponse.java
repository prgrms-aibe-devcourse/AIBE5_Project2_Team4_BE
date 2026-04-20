package com.ieum.ansimdonghaeng.domain.verification.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.time.LocalDateTime;

public record VerificationResponse(
        Long verificationId,
        Long freelancerProfileId,
        VerificationType type,
        VerificationStatus status,
        String requestMessage,
        Long reviewedByUserId,
        LocalDateTime requestedAt,
        String rejectReason,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static VerificationResponse from(Verification verification) {
        return new VerificationResponse(
                verification.getId(),
                verification.getFreelancerProfile().getId(),
                verification.getVerificationType(),
                verification.getStatus(),
                verification.getDescription(),
                verification.getReviewedByUser() != null ? verification.getReviewedByUser().getId() : null,
                verification.getRequestedAt(),
                verification.getRejectReason(),
                verification.getReviewedAt(),
                verification.getCreatedAt(),
                verification.getUpdatedAt()
        );
    }
}
