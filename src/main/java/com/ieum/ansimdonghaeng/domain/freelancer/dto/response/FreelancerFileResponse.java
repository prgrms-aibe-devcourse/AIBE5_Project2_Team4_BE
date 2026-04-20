package com.ieum.ansimdonghaeng.domain.freelancer.dto.response;

import com.ieum.ansimdonghaeng.domain.file.support.FileKeySupport;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile;
import java.time.LocalDateTime;

public record FreelancerFileResponse(
        Long fileId,
        Long freelancerProfileId,
        String originalFilename,
        String contentType,
        Long fileSize,
        Integer displayOrder,
        String viewUrl,
        String downloadUrl,
        LocalDateTime uploadedAt
) {

    public static FreelancerFileResponse from(FreelancerFile file) {
        String fileKey = FileKeySupport.portfolioKey(file.getId());
        return new FreelancerFileResponse(
                file.getId(),
                file.getFreelancerProfile().getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getDisplayOrder(),
                FileKeySupport.viewUrl(fileKey),
                FileKeySupport.downloadUrl(fileKey),
                file.getUploadedAt()
        );
    }
}
