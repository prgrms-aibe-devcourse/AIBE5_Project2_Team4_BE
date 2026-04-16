package com.ieum.ansimdonghaeng.domain.verification.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationRequest;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.time.LocalDateTime;

public record VerificationResponse(
        Long verificationRequestId,
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

    public static VerificationResponse from(VerificationRequest request) {
        return new VerificationResponse(
                request.getId(),
                request.getFreelancerProfile().getId(),
                request.getType(),
                request.getStatus(),
                request.getRequestMessage(),
                request.getReviewedByUserId(),
                request.getRequestedAt(),
                request.getRejectReason(),
                request.getReviewedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
