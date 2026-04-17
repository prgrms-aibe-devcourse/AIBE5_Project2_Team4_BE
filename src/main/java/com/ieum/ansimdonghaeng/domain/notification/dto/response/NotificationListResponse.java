package com.ieum.ansimdonghaeng.domain.notification.dto.response;

import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;

public record NotificationListResponse(
        List<NotificationSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        long unreadCount
) {

    public static NotificationListResponse from(Page<Notification> page, long unreadCount) {
        return new NotificationListResponse(
                page.getContent().stream()
                        .map(NotificationSummaryResponse::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                unreadCount
        );
    }
}
