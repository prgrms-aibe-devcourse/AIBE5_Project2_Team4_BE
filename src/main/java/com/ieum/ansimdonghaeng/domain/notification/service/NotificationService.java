package com.ieum.ansimdonghaeng.domain.notification.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationBulkReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationDetailResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationListResponse;
import com.ieum.ansimdonghaeng.domain.notification.dto.response.NotificationReadResponse;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationListResponse getMyNotifications(Long currentUserId, Boolean isRead, Pageable pageable) {
        requireActiveUser(currentUserId);
        Page<Notification> page = isRead == null
                ? notificationRepository.findAllByUser_IdOrderByCreatedAtDesc(currentUserId, pageable)
                : notificationRepository.findAllByUser_IdAndReadYnOrderByCreatedAtDesc(currentUserId, isRead, pageable);
        return NotificationListResponse.from(
                page,
                notificationRepository.countByUser_IdAndReadYnFalse(currentUserId)
        );
    }

    public NotificationDetailResponse getNotification(Long currentUserId, Long notificationId) {
        requireActiveUser(currentUserId);
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        return NotificationDetailResponse.from(notification);
    }

    @Transactional
    public NotificationReadResponse markAsRead(Long currentUserId, Long notificationId) {
        requireActiveUser(currentUserId);
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markRead(LocalDateTime.now());
        return new NotificationReadResponse(notification.getId(), notification.getReadYn());
    }

    @Transactional
    public NotificationBulkReadResponse markAllAsRead(Long currentUserId) {
        requireActiveUser(currentUserId);
        int readCount = notificationRepository.markAllAsRead(currentUserId, LocalDateTime.now());
        return new NotificationBulkReadResponse(readCount);
    }

    @Transactional
    public void notifyProposalReceived(Proposal proposal) {
        User freelancerUser = proposal.getFreelancerProfile().getUser();
        if (!Boolean.TRUE.equals(freelancerUser.getActiveYn())) {
            return;
        }
        notificationRepository.save(Notification.proposalReceived(
                freelancerUser,
                proposal,
                "새 제안이 도착했습니다.",
                proposal.getProject().getTitle() + " 프로젝트에 대한 새 제안이 도착했습니다."
        ));
    }

    @Transactional
    public void notifyProposalAccepted(Proposal proposal) {
        User ownerUser = proposal.getProject().getOwnerUser();
        if (ownerUser == null || !Boolean.TRUE.equals(ownerUser.getActiveYn())) {
            return;
        }
        notificationRepository.save(Notification.proposalAccepted(
                ownerUser,
                proposal,
                "제안이 수락되었습니다.",
                proposal.getFreelancerProfile().getUser().getName() + " 님이 제안을 수락했습니다."
        ));
    }

    @Transactional
    public void notifyProjectStatusChanged(Project project, Proposal acceptedProposal) {
        List<Notification> notifications = new ArrayList<>();
        User ownerUser = project.getOwnerUser();
        User freelancerUser = acceptedProposal.getFreelancerProfile().getUser();
        String content = project.getTitle() + " 프로젝트 상태가 " + project.getStatus().name() + "(으)로 변경되었습니다.";

        if (ownerUser != null && Boolean.TRUE.equals(ownerUser.getActiveYn())) {
            notifications.add(Notification.projectStatusChanged(
                    ownerUser,
                    project,
                    "프로젝트 상태가 변경되었습니다.",
                    content
            ));
        }
        if (freelancerUser != null
                && Boolean.TRUE.equals(freelancerUser.getActiveYn())
                && (ownerUser == null || !freelancerUser.getId().equals(ownerUser.getId()))) {
            notifications.add(Notification.projectStatusChanged(
                    freelancerUser,
                    project,
                    "프로젝트 상태가 변경되었습니다.",
                    content
            ));
        }
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void notifyReviewRequest(Project project) {
        User ownerUser = project.getOwnerUser();
        if (ownerUser == null || !Boolean.TRUE.equals(ownerUser.getActiveYn())) {
            return;
        }
        notificationRepository.save(Notification.reviewRequest(
                ownerUser,
                project,
                "리뷰를 남겨주세요.",
                project.getTitle() + " 프로젝트가 완료되었습니다. 리뷰를 작성할 수 있습니다."
        ));
    }

    @Transactional
    public void deleteNotification(Long currentUserId, Long notificationId) {
        requireActiveUser(currentUserId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        notificationRepository.delete(notification);
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
