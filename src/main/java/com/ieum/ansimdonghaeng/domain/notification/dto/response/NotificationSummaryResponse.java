package com.ieum.ansimdonghaeng.domain.notification.dto.response;

import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import java.time.LocalDateTime;

public record NotificationSummaryResponse(
        Long notificationId,
        String notificationType,
        String title,
        String content,
        Boolean readYn,
        Long relatedProjectId,
        Long relatedProposalId,
        Long relatedReviewId,
        Long relatedNoticeId,
        Long relatedVerificationId,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {

    public static NotificationSummaryResponse from(Notification notification) {
        return new NotificationSummaryResponse(
                notification.getId(),
                notification.getNotificationType().name(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReadYn(),
                notification.getRelatedProjectId(),
                notification.getRelatedProposalId(),
                notification.getRelatedReviewId(),
                notification.getRelatedNoticeId(),
                notification.getRelatedVerificationId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
