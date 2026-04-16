package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.time.LocalDateTime;
import java.util.List;

public record AdminVerificationDetailResponse(
        Long verificationId,
        Long freelancerProfileId,
        AdminFreelancerSummaryResponse freelancer,
        VerificationType verificationType,
        VerificationStatus status,
        String description,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        AdminUserSummaryResponse reviewedBy,
        String rejectReason,
        List<FileSummaryResponse> files
) {

    public record FileSummaryResponse(
            Long verificationFileId,
            String originalName,
            String storedName,
            String fileUrl,
            String contentType,
            Long fileSize,
            LocalDateTime uploadedAt
    ) {
    }
}
