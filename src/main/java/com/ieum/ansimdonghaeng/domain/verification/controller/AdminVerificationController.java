package com.ieum.ansimdonghaeng.domain.verification.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationReviewRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationListResponse;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationResponse;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.service.VerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/verifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationController {

    private final VerificationService verificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<VerificationListResponse>> getVerifications(
            @RequestParam(required = false) VerificationStatus status,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.getVerificationsForAdmin(status, page, size)));
    }

    @PatchMapping("/{verificationRequestId}/approve")
    public ResponseEntity<ApiResponse<VerificationResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationRequestId,
            @Valid @RequestBody VerificationReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.approveForAdmin(userDetails.getUserId(), verificationRequestId, request)
        ));
    }

    @PatchMapping("/{verificationRequestId}/reject")
    public ResponseEntity<ApiResponse<VerificationResponse>> reject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationRequestId,
            @Valid @RequestBody VerificationReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.rejectForAdmin(userDetails.getUserId(), verificationRequestId, request)
        ));
    }
}
