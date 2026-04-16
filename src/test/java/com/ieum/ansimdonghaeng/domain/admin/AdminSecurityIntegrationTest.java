package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class AdminSecurityIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminEndpointsRejectUnauthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/verifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/projects")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/freelancers")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/reports")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/reviews")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/admin/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("title", "title", "content", "content"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointsRejectNonAdminUsers() throws Exception {
        User user = saveUser("user@test.com", "user", UserRole.USER);
        User freelancer = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        saveFreelancerProfile(freelancer, true, true);

        mockMvc.perform(get("/api/v1/admin/dashboard").with(userPrincipal(user))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/verifications").with(userPrincipal(user))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/projects").with(userPrincipal(user))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/freelancers").with(freelancerPrincipal(freelancer))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/reports").with(userPrincipal(user))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/reviews").with(userPrincipal(user))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(userPrincipal(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("title", "title", "content", "content"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsAllowAdminUsers() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);

        mockMvc.perform(get("/api/v1/admin/dashboard").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/verifications").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/projects").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/freelancers").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/reports").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/reviews").with(adminPrincipal(admin))).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("title", "title", "content", "content"))))
                .andExpect(status().isOk());
    }
}
