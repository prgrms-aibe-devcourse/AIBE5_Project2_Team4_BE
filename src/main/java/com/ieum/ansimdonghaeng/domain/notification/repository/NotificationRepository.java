package com.ieum.ansimdonghaeng.domain.notification.repository;

import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findAllByUser_IdAndReadYnOrderByCreatedAtDesc(Long userId, Boolean readYn, Pageable pageable);

    Optional<Notification> findByIdAndUser_Id(Long notificationId, Long userId);

    long countByUser_IdAndReadYnFalse(Long userId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Notification notification
            set notification.readYn = true,
                notification.readAt = :readAt
            where notification.user.id = :userId
              and notification.readYn = false
            """)
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Notification notification
            set notification.title = :title,
                notification.content = :content
            where notification.relatedNoticeId = :noticeId
            """)
    int updateNoticeSnapshot(@Param("noticeId") Long noticeId,
                             @Param("title") String title,
                             @Param("content") String content);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from Notification notification
            where notification.relatedNoticeId = :noticeId
            """)
    int deleteAllByRelatedNoticeId(@Param("noticeId") Long noticeId);
}
