package com.ieum.ansimdonghaeng.domain.project.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import java.time.LocalDateTime;

// 프로젝트 상세 화면에 필요한 전체 정보를 담는다.
public record ProjectDetailResponse(
        Long projectId,
        Long ownerUserId,
        String title,
        String projectTypeCode,
        String serviceRegionCode,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        String serviceAddress,
        String serviceDetailAddress,
        String requestDetail,
        ProjectStatus status,
        LocalDateTime acceptedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        String cancelledReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // 엔티티를 상세 응답 DTO로 변환한다.
    public static ProjectDetailResponse from(Project project) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getOwnerUserId(),
                project.getTitle(),
                project.getProjectTypeCode(),
                project.getServiceRegionCode(),
                project.getRequestedStartAt(),
                project.getRequestedEndAt(),
                project.getServiceAddress(),
                project.getServiceDetailAddress(),
                project.getRequestDetail(),
                project.getStatus(),
                project.getAcceptedAt(),
                project.getStartedAt(),
                project.getCompletedAt(),
                project.getCancelledAt(),
                project.getCancelledReason(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
