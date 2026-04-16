package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminFreelancerActiveRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminFreelancerVisibilityRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerStateResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminFreelancerService;
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
@RequestMapping("/api/v1/admin/freelancers")
public class AdminFreelancerController {

    private final AdminFreelancerService adminFreelancerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminFreelancerListItemResponse>>> getFreelancers(
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String projectType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminFreelancerService.getFreelancers(verified, keyword, region, projectType, pageable)
        ));
    }

    @GetMapping("/{freelancerProfileId}")
    public ResponseEntity<ApiResponse<AdminFreelancerDetailResponse>> getFreelancer(@PathVariable Long freelancerProfileId) {
        return ResponseEntity.ok(ApiResponse.success(adminFreelancerService.getFreelancer(freelancerProfileId)));
    }

    @PatchMapping("/{freelancerProfileId}/visibility")
    public ResponseEntity<ApiResponse<AdminFreelancerStateResponse>> updateVisibility(
            @PathVariable Long freelancerProfileId,
            @Valid @RequestBody AdminFreelancerVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminFreelancerService.updateVisibility(freelancerProfileId, request)
        ));
    }

    @PatchMapping("/{freelancerProfileId}/active")
    public ResponseEntity<ApiResponse<AdminFreelancerStateResponse>> updateActive(
            @PathVariable Long freelancerProfileId,
            @Valid @RequestBody AdminFreelancerActiveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminFreelancerService.updateActive(freelancerProfileId, request)
        ));
    }
}
