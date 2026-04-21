package com.ieum.ansimdonghaeng.domain.notification.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listAndReadNotificationsSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "published notice",
                                "content", "notice content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "second notice",
                                "content", "second content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());

        java.util.List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(savedNotification -> savedNotification.getUser().getId().equals(user.getId()))
                .toList();
        Notification notification = notifications.stream()
                .max(java.util.Comparator.comparing(Notification::getCreatedAt))
                .orElseThrow();

        mockMvc.perform(get("/api/v1/notifications").with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.unreadCount").value(2))
                .andExpect(jsonPath("$.data.content[0].notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.content[0].notificationType").value("NOTICE"))
                .andExpect(jsonPath("$.data.content[0].isRead").value(false));

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", notification.getId())
                        .with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.notificationType").value("NOTICE"))
                .andExpect(jsonPath("$.data.isRead").value(false));

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notification.getId())
                        .with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.isRead").value(true));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("isRead", "true")
                        .with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.content[0].isRead").value(true));

        mockMvc.perform(patch("/api/v1/notifications/read-all").with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readCount").value(1));
    }

    @Test
    void notificationOwnershipIsEnforced() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User otherUser = saveUser("other@test.com", "other", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "published notice",
                                "content", "notice content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());

        Notification ownersNotification = notificationRepository.findAll().stream()
                .filter(notification -> notification.getUser().getId().equals(owner.getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", ownersNotification.getId())
                        .with(userPrincipal(otherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOTIFICATION_404_1"));

        mockMvc.perform(get("/api/v1/notifications/{notificationId}", ownersNotification.getId())
                        .with(userPrincipal(otherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOTIFICATION_404_1"));
    }

    @Test
    void deleteNotificationSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "notice title",
                                "content", "notice content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());

        Notification notification = notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/v1/notifications/{notificationId}", notification.getId())
                        .with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteNotificationReturns404WhenNotFound() throws Exception {
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(delete("/api/v1/notifications/{notificationId}", Long.MAX_VALUE)
                        .with(userPrincipal(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOTIFICATION_404_1"));
    }

    @Test
    void deleteNotificationReturns403WhenNotOwner() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User otherUser = saveUser("other@test.com", "other", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "notice title",
                                "content", "notice content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());

        Notification ownersNotification = notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(owner.getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/v1/notifications/{notificationId}", ownersNotification.getId())
                        .with(userPrincipal(otherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }
}
