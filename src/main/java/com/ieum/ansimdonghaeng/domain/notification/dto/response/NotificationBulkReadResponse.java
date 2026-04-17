package com.ieum.ansimdonghaeng.domain.notification.dto.response;

public record NotificationBulkReadResponse(
        int updatedCount,
        long unreadCount
) {
}
