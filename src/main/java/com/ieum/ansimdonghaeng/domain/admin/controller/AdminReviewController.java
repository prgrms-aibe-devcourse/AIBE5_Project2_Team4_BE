package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminReviewBlindRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReviewListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReviewVisibilityResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminReviewService;
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
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReviewListItemResponse>>> getReviews(
            @RequestParam(required = false) Boolean blinded,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.getReviews(blinded, keyword, pageable)));
    }

    @PatchMapping("/{reviewId}/blind")
    public ResponseEntity<ApiResponse<AdminReviewVisibilityResponse>> blind(
            @PathVariable Long reviewId,
            @Valid @RequestBody AdminReviewBlindRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.blind(reviewId, request)));
    }

    @PatchMapping("/{reviewId}/unblind")
    public ResponseEntity<ApiResponse<AdminReviewVisibilityResponse>> unblind(@PathVariable Long reviewId) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.unblind(reviewId)));
    }
}
