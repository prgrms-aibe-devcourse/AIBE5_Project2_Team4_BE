package com.ieum.ansimdonghaeng.domain.project.dto.response;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.domain.Page;

// 공통 ApiResponse 내부에서 프로젝트 페이지 정보를 전달한다.
public record ProjectListResponse(
        List<ProjectSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    // Page 결과를 API 계약에 맞는 목록 응답 DTO로 변환한다.
    public static ProjectListResponse from(Page<Project> projectPage) {
        return new ProjectListResponse(
                projectPage.getContent().stream().map(ProjectSummaryResponse::from).toList(),
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.hasNext()
        );
    }
}
