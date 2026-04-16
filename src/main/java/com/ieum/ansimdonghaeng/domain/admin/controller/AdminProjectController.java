package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminProjectDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminProjectSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminProjectService;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCancelRequest;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/projects")
public class AdminProjectController {

    private final AdminProjectService adminProjectService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminProjectSummaryResponse>>> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String writerKeyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminProjectService.getProjects(status, keyword, writerKeyword, pageable)
        ));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<AdminProjectDetailResponse>> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(adminProjectService.getProject(projectId)));
    }

    @PatchMapping("/{projectId}/cancel")
    public ResponseEntity<ApiResponse<AdminProjectDetailResponse>> cancelProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectCancelRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminProjectService.cancelProject(projectId, request)));
    }
}
