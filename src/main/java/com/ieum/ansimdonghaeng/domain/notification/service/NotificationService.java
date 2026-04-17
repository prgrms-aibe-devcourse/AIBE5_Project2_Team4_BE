package com.ieum.ansimdonghaeng.domain.notification.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationBulkReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationListResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationListResponse getMyNotifications(Long currentUserId, Pageable pageable) {
        requireActiveUser(currentUserId);
        return NotificationListResponse.from(
                notificationRepository.findAllByUser_IdOrderByCreatedAtDesc(currentUserId, pageable),
                notificationRepository.countByUser_IdAndReadYnFalse(currentUserId)
        );
    }

    @Transactional
    public NotificationReadResponse markAsRead(Long currentUserId, Long notificationId) {
        requireActiveUser(currentUserId);
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markRead(LocalDateTime.now());
        return new NotificationReadResponse(notification.getId(), notification.getReadYn(), notification.getReadAt());
    }

    @Transactional
    public NotificationBulkReadResponse markAllAsRead(Long currentUserId) {
        requireActiveUser(currentUserId);
        int updatedCount = notificationRepository.markAllAsRead(currentUserId, LocalDateTime.now());
        long unreadCount = notificationRepository.countByUser_IdAndReadYnFalse(currentUserId);
        return new NotificationBulkReadResponse(updatedCount, unreadCount);
    }

    private User requireActiveUser(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));
        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }
        return user;
    }
}
