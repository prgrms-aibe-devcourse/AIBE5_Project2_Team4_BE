package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
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
class AdminVerificationControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getVerificationsSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, false, true);
        Verification verification = saveVerification(profile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);
        saveVerificationFile(verification);

        mockMvc.perform(get("/api/v1/admin/verifications")
                        .with(adminPrincipal(admin))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].verificationId").value(verification.getId()))
                .andExpect(jsonPath("$.data.content[0].hasFiles").value(true));
    }

    @Test
    void approveVerificationSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, false, true);
        Verification verification = saveVerification(profile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        mockMvc.perform(patch("/api/v1/admin/verifications/{verificationId}/approve", verification.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("comment", "approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewedBy.userId").value(admin.getId()))
                .andExpect(jsonPath("$.data.freelancer.verifiedYn").value(true));
    }

    @Test
    void rejectVerificationSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, false, true);
        Verification verification = saveVerification(profile, VerificationType.CAREGIVER, VerificationStatus.PENDING, null, null);

        mockMvc.perform(patch("/api/v1/admin/verifications/{verificationId}/reject", verification.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "missing file"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectReason").value("missing file"));
    }

    @Test
    void rejectAlreadyReviewedVerificationFails() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        Verification verification = saveVerification(profile, VerificationType.LICENSE, VerificationStatus.APPROVED, admin, null);

        mockMvc.perform(patch("/api/v1/admin/verifications/{verificationId}/reject", verification.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "late reject"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_409_1"));
    }
}
