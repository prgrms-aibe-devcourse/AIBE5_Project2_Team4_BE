package com.ieum.ansimdonghaeng.domain.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationReadResponse(
        Long notificationId,
        Boolean readYn,
        LocalDateTime readAt
) {
}
