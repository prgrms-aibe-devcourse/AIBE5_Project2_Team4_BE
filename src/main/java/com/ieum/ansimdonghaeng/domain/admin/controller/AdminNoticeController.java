package com.ieum.ansimdonghaeng.domain.admin.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminNoticeCreateRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminNoticeResponse;
import com.ieum.ansimdonghaeng.domain.admin.service.AdminNoticeService;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminAuthenticationSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notices")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminNoticeResponse>> createNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminNoticeCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminNoticeService.createNotice(AdminAuthenticationSupport.currentUserId(userDetails), request)
        ));
    }

    @PatchMapping("/{noticeId}/publish")
    public ResponseEntity<ApiResponse<AdminNoticeResponse>> publishNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(ApiResponse.success(adminNoticeService.publishNotice(noticeId)));
    }
}
