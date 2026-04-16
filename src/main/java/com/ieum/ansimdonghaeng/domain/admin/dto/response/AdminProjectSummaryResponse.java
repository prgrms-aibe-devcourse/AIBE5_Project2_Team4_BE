package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import java.time.LocalDateTime;

public record AdminProjectSummaryResponse(
        Long projectId,
        String title,
        String projectTypeCode,
        ProjectStatus status,
        AdminUserSummaryResponse owner,
        AdminFreelancerSummaryResponse acceptedFreelancer,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
