package com.ieum.ansimdonghaeng.domain.verification.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerificationControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pendingVerificationAllowsFileUpload() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, false, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "career-proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-proof".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .file(verificationFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.verificationId").value(verification.getId()));
    }

    @Test
    void approvedVerificationRejectsFileUpload() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        User adminUser = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.APPROVED, adminUser, null);

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "career-proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-proof".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .file(verificationFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_400_1"));
    }

    @Test
    void rejectedVerificationRejectsFileUpload() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        User adminUser = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, false, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.REJECTED, adminUser, "reject");

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "career-proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-proof".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .file(verificationFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_400_1"));
    }

    @Test
    void approvedVerificationRejectsFileDeletion() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        User adminUser = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.APPROVED, adminUser, null);
        var file = saveVerificationFile(verification);

        mockMvc.perform(delete("/api/v1/freelancers/me/verifications/files/{fileId}", file.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_400_1"));

        org.assertj.core.api.Assertions.assertThat(verificationFileRepository.existsById(file.getId())).isTrue();
    }

    @Test
    void rejectedVerificationRejectsFileDeletion() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        User adminUser = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, false, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.REJECTED, adminUser, "reject");
        var file = saveVerificationFile(verification);

        mockMvc.perform(delete("/api/v1/freelancers/me/verifications/files/{fileId}", file.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_400_1"));

        org.assertj.core.api.Assertions.assertThat(verificationFileRepository.existsById(file.getId())).isTrue();
    }

    @Test
    void reviewedVerificationStillAllowsFileListing() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        User adminUser = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.APPROVED, adminUser, null);
        var file = saveVerificationFile(verification);

        mockMvc.perform(get("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].verificationFileId").value(file.getId()));
    }
}
