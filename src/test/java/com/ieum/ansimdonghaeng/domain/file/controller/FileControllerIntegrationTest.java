package com.ieum.ansimdonghaeng.domain.file.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "app.file-storage.base-dir=./target/file-controller-test-storage")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileControllerIntegrationTest extends AdminIntegrationTestSupport {

    private static final Path TEST_STORAGE_DIR = Path.of("./target/file-controller-test-storage")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @AfterEach
    void cleanStorage() throws Exception {
        if (!Files.exists(TEST_STORAGE_DIR)) {
            return;
        }

        try (var paths = Files.walk(TEST_STORAGE_DIR)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                            // Test storage cleanup should not hide assertion failures.
                        }
                    });
        }
    }

    @Test
    void portfolioFileCanBeViewedPubliclyButVerificationFileRequiresAuthorization() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        MockMultipartFile portfolioFile = new MockMultipartFile(
                "file",
                "portfolio.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "portfolio-file".getBytes()
        );
        MvcResult portfolioResult = mockMvc.perform(multipart("/api/v1/freelancers/me/files")
                        .file(portfolioFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isCreated())
                .andReturn();
        String portfolioViewUrl = objectMapper.readTree(portfolioResult.getResponse().getContentAsString())
                .path("data")
                .path("viewUrl")
                .asText();

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-proof".getBytes()
        );
        MvcResult verificationResult = mockMvc.perform(multipart("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .file(verificationFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isCreated())
                .andReturn();
        String verificationViewUrl = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("data")
                .path("viewUrl")
                .asText();
        String verificationDownloadUrl = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("data")
                .path("downloadUrl")
                .asText();

        mockMvc.perform(get(portfolioViewUrl))
                .andExpect(status().isOk())
                .andExpect(content().bytes("portfolio-file".getBytes()));

        mockMvc.perform(get(verificationViewUrl))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FILE_404_1"));

        mockMvc.perform(get(verificationDownloadUrl).with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes("%PDF-proof".getBytes()));
    }

    @Test
    void adminCanViewAndDownloadVerificationFileOwnedByAnotherUser() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer-admin-file@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-admin-proof".getBytes(StandardCharsets.UTF_8)
        );
        MvcResult verificationResult = mockMvc.perform(multipart("/api/v1/freelancers/me/verifications/{verificationId}/files", verification.getId())
                        .file(verificationFile)
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isCreated())
                .andReturn();
        String verificationViewUrl = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("data")
                .path("viewUrl")
                .asText();
        String verificationDownloadUrl = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("data")
                .path("downloadUrl")
                .asText();

        var savedFile = verificationFileRepository.findAllByVerificationIdAndUserId(
                verification.getId(),
                freelancerUser.getId()
        ).get(0);
        assertThat(savedFile.getFileUrl()).startsWith("freelancers/");
        assertThat(Path.of(savedFile.getFileUrl()).isAbsolute()).isFalse();

        mockMvc.perform(get(verificationViewUrl).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(content().bytes("%PDF-admin-proof".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get(verificationDownloadUrl).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().bytes("%PDF-admin-proof".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void legacyAbsoluteVerificationFilePathResolvesAgainstCurrentStorage() throws Exception {
        User admin = saveUser("admin-legacy@test.com", "admin", UserRole.ADMIN);
        User freelancerUser = saveUser("freelancer-legacy@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var verification = saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        byte[] content = "legacy-proof".getBytes(StandardCharsets.UTF_8);
        Path currentStoragePath = TEST_STORAGE_DIR
                .resolve("freelancers")
                .resolve(String.valueOf(freelancerProfile.getId()))
                .resolve("verifications")
                .resolve(String.valueOf(verification.getId()))
                .resolve("legacy-proof.txt");
        Files.createDirectories(currentStoragePath.getParent());
        Files.write(currentStoragePath, content);

        String legacyAbsolutePath = "D:\\AIBE5_PJT2_TEAM4\\storage\\local\\freelancers\\"
                + freelancerProfile.getId()
                + "\\verifications\\"
                + verification.getId()
                + "\\legacy-proof.txt";
        VerificationFile file = verificationFileRepository.saveAndFlush(VerificationFile.create(
                verification,
                "legacy-proof.txt",
                "legacy-proof.txt",
                legacyAbsolutePath,
                MediaType.TEXT_PLAIN_VALUE,
                (long) content.length,
                LocalDateTime.of(2026, 4, 17, 9, 30)
        ));

        mockMvc.perform(get("/api/v1/files/verification-{fileId}", file.getId())
                        .with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(content));
    }
}
