package com.ieum.ansimdonghaeng.domain.verification.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import java.time.LocalDateTime;

public record VerificationFileResponse(
        Long verificationFileId,
        Long verificationRequestId,
        String originalFilename,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt
) {

    public static VerificationFileResponse from(VerificationFile file) {
        return new VerificationFileResponse(
                file.getId(),
                file.getVerificationRequest().getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getUploadedAt()
        );
    }
}
