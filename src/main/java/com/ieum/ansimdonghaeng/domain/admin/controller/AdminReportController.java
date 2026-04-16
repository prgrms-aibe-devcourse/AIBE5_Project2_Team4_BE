package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReportDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReportListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminReportService;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminAuthenticationSupport;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReportListItemResponse>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReasonType reasonType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminReportService.getReports(status, reasonType, pageable)));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.success(adminReportService.getReport(reportId)));
    }

    @PatchMapping("/{reportId}/resolve")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> resolve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminReportService.resolve(AdminAuthenticationSupport.currentUserId(userDetails), reportId)
        ));
    }

    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminReportService.reject(AdminAuthenticationSupport.currentUserId(userDetails), reportId)
        ));
    }
}
