package com.ieum.ansimdonghaeng.domain.verification.dto.response;

import com.ieum.ansimdonghaeng.domain.file.support.FileKeySupport;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import java.time.LocalDateTime;

public record VerificationFileResponse(
        Long verificationFileId,
        Long verificationId,
        String originalFilename,
        String contentType,
        Long fileSize,
        String viewUrl,
        String downloadUrl,
        LocalDateTime uploadedAt
) {

    public static VerificationFileResponse from(VerificationFile file) {
        String fileKey = FileKeySupport.verificationKey(file.getId());
        return new VerificationFileResponse(
                file.getId(),
                file.getVerification().getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                FileKeySupport.viewUrl(fileKey),
                FileKeySupport.downloadUrl(fileKey),
                file.getUploadedAt()
        );
    }
}
