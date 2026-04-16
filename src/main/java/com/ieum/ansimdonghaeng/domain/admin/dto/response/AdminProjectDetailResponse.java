package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import java.time.LocalDateTime;

public record AdminProjectDetailResponse(
        Long projectId,
        String title,
        String projectTypeCode,
        String serviceRegionCode,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        String serviceAddress,
        String serviceDetailAddress,
        String requestDetail,
        ProjectStatus status,
        AdminUserSummaryResponse owner,
        AcceptedProposalResponse acceptedProposal,
        LocalDateTime acceptedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record AcceptedProposalResponse(
            Long proposalId,
            AdminFreelancerSummaryResponse freelancer,
            String message,
            LocalDateTime respondedAt
    ) {
    }
}
