package com.ieum.ansimdonghaeng.domain.notification.controller;

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

        Notification notification = notificationRepository.findAll().stream()
                .filter(savedNotification -> savedNotification.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/v1/notifications").with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.content[0].notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.content[0].notificationType").value("NOTICE"))
                .andExpect(jsonPath("$.data.content[0].readYn").value(false));

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/read", notification.getId())
                        .with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationId").value(notification.getId()))
                .andExpect(jsonPath("$.data.readYn").value(true))
                .andExpect(jsonPath("$.data.readAt").exists());

        mockMvc.perform(patch("/api/v1/notifications/read-all").with(userPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(0))
                .andExpect(jsonPath("$.data.unreadCount").value(0));
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
    }
}
