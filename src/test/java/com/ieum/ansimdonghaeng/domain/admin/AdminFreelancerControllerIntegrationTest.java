package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
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
class AdminFreelancerControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFreelancersSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);

        mockMvc.perform(get("/api/v1/admin/freelancers")
                        .with(adminPrincipal(admin))
                        .param("verified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].freelancerProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.data.content[0].email").value(freelancerUser.getEmail()));
    }

    @Test
    void getFreelancerDetailSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, false, true);
        var file = saveFreelancerFile(profile);
        saveVerification(profile, VerificationType.CAREGIVER, VerificationStatus.PENDING, null, null);

        mockMvc.perform(get("/api/v1/admin/freelancers/{freelancerProfileId}", profile.getId()).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancerProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.data.userId").value(freelancerUser.getId()))
                .andExpect(jsonPath("$.data.recentVerifications[0].verificationId").exists())
                .andExpect(jsonPath("$.data.portfolioFiles[0].fileId").value(file.getId()))
                .andExpect(jsonPath("$.data.portfolioFiles[0].viewUrl").value("/api/v1/files/portfolio-" + file.getId()))
                .andExpect(jsonPath("$.data.portfolioFiles[0].downloadUrl").value("/api/v1/files/portfolio-" + file.getId() + "/download"));
    }

    @Test
    void updateFreelancerVisibilitySuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);

        mockMvc.perform(patch("/api/v1/admin/freelancers/{freelancerProfileId}/visibility", profile.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("publicYn", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicYn").value(false));
    }

    @Test
    void updateFreelancerActiveFailsWhenNotFound() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/freelancers/{freelancerProfileId}/active", 9999L)
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("activeYn", false))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FREELANCER_404_1"));
    }
}
