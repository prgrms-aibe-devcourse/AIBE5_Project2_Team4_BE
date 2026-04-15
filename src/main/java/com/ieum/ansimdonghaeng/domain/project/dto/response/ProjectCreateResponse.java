package com.ieum.ansimdonghaeng.domain.project.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;

// 프로젝트 생성 결과에 필요한 최소 정보만 반환한다.
public record ProjectCreateResponse(
        Long projectId,
        ProjectStatus status
) {

    // 엔티티를 생성 응답 DTO로 변환한다.
    public static ProjectCreateResponse from(Project project) {
        return new ProjectCreateResponse(project.getId(), project.getStatus());
    }
}
