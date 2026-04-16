package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminNoticeCreateRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminNoticeResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminResponseMapper;
import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
import com.ieum.ansimdonghaeng.domain.notice.repository.NoticeRepository;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public AdminNoticeResponse createNotice(Long adminUserId, AdminNoticeCreateRequest request) {
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Admin user was not found."));
        boolean publishNow = Boolean.TRUE.equals(request.publishNow());
        Notice notice = noticeRepository.save(Notice.create(
                adminUser,
                request.title(),
                request.content(),
                publishNow,
                LocalDateTime.now()
        ));
        if (publishNow) {
            fanOutNoticeNotification(notice);
        }
        return toResponse(notice);
    }

    @Transactional
    public AdminNoticeResponse publishNotice(Long noticeId) {
        Notice notice = noticeRepository.findDetailById(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        if (notice.isPublished()) {
            throw new CustomException(ErrorCode.NOTICE_ALREADY_PUBLISHED);
        }
        notice.publish(LocalDateTime.now());
        fanOutNoticeNotification(notice);
        return toResponse(notice);
    }

    private void fanOutNoticeNotification(Notice notice) {
        String content = truncate(notice.getContent(), 2000);
        List<Notification> notifications = userRepository.findAllByActiveYnTrue().stream()
                .map(user -> Notification.notice(user, notice, content))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    private AdminNoticeResponse toResponse(Notice notice) {
        return new AdminNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getPublishedYn(),
                notice.getPublishedAt(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                AdminResponseMapper.toUserSummary(notice.getAdminUser())
        );
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
