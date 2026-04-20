package com.ieum.ansimdonghaeng.domain.project.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
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

    @Operation(summary = "Create project")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectCreateResponse>> createProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectCreateResponse response = projectService.createProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "List my projects")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectListResponse>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ProjectStatus status,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        ProjectListResponse response = projectService.getMyProjects(
                AuthenticatedUserSupport.currentUserId(userDetails),
                status,
                page,
                size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get project detail")
    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        ProjectDetailResponse response = projectService.getProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update project")
    @PatchMapping("/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> updateProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectDetailResponse response = projectService.updateProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Cancel project")
    @PatchMapping("/{projectId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectCancelResponse>> cancelProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectCancelRequest request
    ) {
        ProjectCancelResponse response = projectService.cancelProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{projectId}/start")
    @PreAuthorize("hasAnyRole('FREELANCER','ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> startProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectService.startProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                hasAuthority(userDetails, "ROLE_ADMIN"),
                projectId
        )));
    }

    @PatchMapping("/{projectId}/complete")
    @PreAuthorize("hasAnyRole('FREELANCER','ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> completeProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectService.completeProject(
                AuthenticatedUserSupport.currentUserId(userDetails),
                hasAuthority(userDetails, "ROLE_ADMIN"),
                projectId
        )));
    }

    private boolean hasAuthority(CustomUserDetails userDetails, String authority) {
        return userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }
}
