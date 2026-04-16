package com.ieum.ansimdonghaeng.domain.freelancer.dto.response;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile;
import java.time.LocalDateTime;

public record FreelancerFileResponse(
        Long fileId,
        Long freelancerProfileId,
        String originalFilename,
        String contentType,
        Long fileSize,
        Integer displayOrder,
        LocalDateTime uploadedAt
) {

    public static FreelancerFileResponse from(FreelancerFile file) {
        return new FreelancerFileResponse(
                file.getId(),
                file.getFreelancerProfile().getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getDisplayOrder(),
                file.getUploadedAt()
        );
    }
}
