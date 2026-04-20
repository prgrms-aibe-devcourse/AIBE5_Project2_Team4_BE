package com.ieum.ansimdonghaeng.domain.file.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
}
