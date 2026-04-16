package com.ieum.ansimdonghaeng.domain.admin;

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
