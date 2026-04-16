package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.time.LocalDateTime;

public record AdminVerificationListItemResponse(
        Long verificationId,
        Long freelancerProfileId,
        Long userId,
        String applicantName,
        String applicantEmail,
        VerificationType verificationType,
        VerificationStatus status,
        String descriptionSummary,
        Boolean hasFiles,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt
) {
}
