package com.ieum.ansimdonghaeng.domain.notification.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationBulkReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationListResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(AuthenticatedUserSupport.currentUserId(userDetails), pageable)
        ));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markAsRead(AuthenticatedUserSupport.currentUserId(userDetails), notificationId)
        ));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<NotificationBulkReadResponse>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markAllAsRead(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }
}
