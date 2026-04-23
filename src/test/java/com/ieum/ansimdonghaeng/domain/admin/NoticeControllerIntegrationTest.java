package com.ieum.ansimdonghaeng.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
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
class NoticeControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createNoticeSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "new notice",
                                "content", "notice content",
                                "publishNow", false
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("new notice"))
                .andExpect(jsonPath("$.data.publishedYn").value(false));
    }

    @Test
    void publishNoticeSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        Notice notice = saveNotice(admin, false);

        mockMvc.perform(patch("/api/v1/admin/notices/{noticeId}/publish", notice.getId())
                        .with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeId").value(notice.getId()))
                .andExpect(jsonPath("$.data.publishedYn").value(true))
                .andExpect(jsonPath("$.data.publishedAt").exists());
    }

    @Test
    void publishAlreadyPublishedNoticeFails() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        Notice notice = saveNotice(admin, true);

        mockMvc.perform(patch("/api/v1/admin/notices/{noticeId}/publish", notice.getId())
                        .with(adminPrincipal(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("NOTICE_409_1"));
    }

    @Test
    void updateNoticeSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "before notice",
                                "content", "before content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());
        Notice notice = noticeRepository.findAll().get(0);

        mockMvc.perform(patch("/api/v1/admin/notices/{noticeId}", notice.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "updated notice",
                                "content", "updated content"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeId").value(notice.getId()))
                .andExpect(jsonPath("$.data.title").value("updated notice"))
                .andExpect(jsonPath("$.data.content").value("updated content"))
                .andExpect(jsonPath("$.data.publishedYn").value(true));

        assertThat(noticeRepository.findById(notice.getId()).orElseThrow().getTitle())
                .isEqualTo("updated notice");
        assertThat(notificationRepository.findAll().stream()
                .filter(notification -> notification.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow()
                .getTitle())
                .isEqualTo("updated notice");
    }

    @Test
    void updateNoticeReturns400WhenNoFields() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        Notice notice = saveNotice(admin, false);

        mockMvc.perform(patch("/api/v1/admin/notices/{noticeId}", notice.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("NOTICE_400_1"));
    }

    @Test
    void deleteNoticeSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "notice to delete",
                                "content", "notice content",
                                "publishNow", true
                        ))))
                .andExpect(status().isOk());
        Notice notice = noticeRepository.findAll().get(0);

        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notification.getUser().getId().equals(user.getId())
                        && notice.getId().equals(notification.getRelatedNoticeId())))
                .isTrue();

        mockMvc.perform(delete("/api/v1/admin/notices/{noticeId}", notice.getId())
                        .with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(noticeRepository.findById(notice.getId())).isEmpty();
        assertThat(notificationRepository.findAll().stream()
                .anyMatch(notification -> notice.getId().equals(notification.getRelatedNoticeId())))
                .isFalse();

        mockMvc.perform(get("/api/v1/notices/{noticeId}", notice.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOTICE_404_1"));
    }

    @Test
    void deleteNoticeReturns404WhenNotFound() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);

        mockMvc.perform(delete("/api/v1/admin/notices/{noticeId}", Long.MAX_VALUE)
                        .with(adminPrincipal(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOTICE_404_1"));
    }

    @Test
    void publicNoticeListAndDetailSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        Notice notice = saveNotice(admin, true);

        mockMvc.perform(get("/api/v1/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].noticeId").value(notice.getId()));

        mockMvc.perform(get("/api/v1/notices/{noticeId}", notice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.noticeId").value(notice.getId()))
                .andExpect(jsonPath("$.data.title").value("notice title"));
    }
}
