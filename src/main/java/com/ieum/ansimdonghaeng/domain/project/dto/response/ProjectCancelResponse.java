package com.ieum.ansimdonghaeng.domain.project.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import java.time.LocalDateTime;

// 프로젝트 취소 결과를 바로 확인할 수 있게 필요한 필드만 반환한다.
public record ProjectCancelResponse(
        Long projectId,
        ProjectStatus status,
        LocalDateTime cancelledAt,
        String cancelledReason
) {

    // 엔티티를 취소 응답 DTO로 변환한다.
    public static ProjectCancelResponse from(Project project) {
        return new ProjectCancelResponse(
                project.getId(),
                project.getStatus(),
                project.getCancelledAt(),
                project.getCancelledReason()
        );
    }
}
