package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminVerificationApproveRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminVerificationRejectRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminVerificationDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminVerificationListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminVerificationService;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminAuthenticationSupport;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/verifications")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminVerificationListItemResponse>>> getVerifications(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) VerificationType verificationType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminVerificationService.getVerifications(status, verificationType, keyword, pageable)
        ));
    }

    @GetMapping("/{verificationId}")
    public ResponseEntity<ApiResponse<AdminVerificationDetailResponse>> getVerification(@PathVariable Long verificationId) {
        return ResponseEntity.ok(ApiResponse.success(adminVerificationService.getVerification(verificationId)));
    }

    @PatchMapping("/{verificationId}/approve")
    public ResponseEntity<ApiResponse<AdminVerificationDetailResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationId,
            @Valid @RequestBody(required = false) AdminVerificationApproveRequest request
    ) {
        AdminVerificationApproveRequest safeRequest = request == null ? new AdminVerificationApproveRequest(null) : request;
        return ResponseEntity.ok(ApiResponse.success(
                adminVerificationService.approve(
                        AdminAuthenticationSupport.currentUserId(userDetails),
                        verificationId,
                        safeRequest
                )
        ));
    }

    @PatchMapping("/{verificationId}/reject")
    public ResponseEntity<ApiResponse<AdminVerificationDetailResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationId,
            @Valid @RequestBody AdminVerificationRejectRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminVerificationService.reject(
                        AdminAuthenticationSupport.currentUserId(userDetails),
                        verificationId,
                        request
                )
        ));
    }
}
