package com.ieum.ansimdonghaeng.domain.project.controller;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCancelRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCreateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectUpdateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCancelResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCreateResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectDetailResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectListResponse;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 현재 로그인한 일반 사용자의 프로젝트 요청을 생성한다.
    @Operation(summary = "프로젝트 생성")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectCreateResponse>> createProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectCreateResponse response = projectService.createProject(currentUserId(userDetails), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // 현재 로그인한 사용자가 소유한 프로젝트만 페이지 단위로 조회한다.
    @Operation(summary = "내 프로젝트 목록 조회")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectListResponse>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ProjectStatus status,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        ProjectListResponse response = projectService.getMyProjects(currentUserId(userDetails), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 프로젝트 작성자 본인만 상세 정보를 조회할 수 있다.
    @Operation(summary = "프로젝트 상세 조회")
    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        ProjectDetailResponse response = projectService.getProject(currentUserId(userDetails), projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // REQUESTED 상태의 프로젝트만 부분 수정할 수 있다.
    @Operation(summary = "프로젝트 수정")
    @PatchMapping("/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> updateProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectDetailResponse response = projectService.updateProject(currentUserId(userDetails), projectId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // REQUESTED 상태의 프로젝트만 취소할 수 있다.
    @Operation(summary = "프로젝트 취소")
    @PatchMapping("/{projectId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectCancelResponse>> cancelProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectCancelRequest request
    ) {
        ProjectCancelResponse response = projectService.cancelProject(currentUserId(userDetails), projectId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 현재 스켈레톤 보안 구조에서는 principal에 userId가 반드시 있어야 owner 검증이 가능하다.
    private Long currentUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Authenticated user id is required.");
        }
        return userDetails.getUserId();
    }
}
