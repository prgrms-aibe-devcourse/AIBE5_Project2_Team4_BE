package com.ieum.ansimdonghaeng.domain.notification.repository;

import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
