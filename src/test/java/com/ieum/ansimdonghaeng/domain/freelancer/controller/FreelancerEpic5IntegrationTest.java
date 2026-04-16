package com.ieum.ansimdonghaeng.domain.freelancer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.request.FreelancerProfileUpsertRequest;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerFileRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationCreateRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationReviewRequest;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRequestRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FreelancerEpic5IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    private FreelancerFileRepository freelancerFileRepository;

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private VerificationFileRepository verificationFileRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        freelancerFileRepository.deleteAll();
        verificationFileRepository.deleteAll();
        verificationRequestRepository.deleteAll();
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("freelancer can create profile, request verification, and upload file")
    void freelancerEpic5Flow() throws Exception {
        userRepository.save(createUser("freelancer-epic5@test.com", "ROLE_USER"));
        String accessToken = login("freelancer-epic5@test.com");

        FreelancerProfileUpsertRequest profileRequest = new FreelancerProfileUpsertRequest(
                "career",
                true,
                true,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        );

        mockMvc.perform(post("/api/v1/freelancers/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_FREELANCER"))
                .andExpect(jsonPath("$.data.verifiedYn").value(false));

        MvcResult verificationResult = mockMvc.perform(post("/api/v1/freelancers/me/verifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerificationCreateRequest(VerificationType.CAREER, "please verify")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        Long verificationRequestId = objectMapper.readTree(verificationResult.getResponse().getContentAsString())
                .path("data")
                .path("verificationRequestId")
                .asLong();

        mockMvc.perform(post("/api/v1/freelancers/me/verifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerificationCreateRequest(VerificationType.CAREER, "duplicate")
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("VERIFICATION_409_1"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "portfolio.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "portfolio".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/files")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalFilename").value("portfolio.txt"))
                .andExpect(jsonPath("$.data.displayOrder").value(1));

        MockMultipartFile verificationFile = new MockMultipartFile(
                "file",
                "career-proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-proof".getBytes()
        );

        MvcResult verificationFileResult = mockMvc.perform(
                        multipart("/api/v1/freelancers/me/verifications/{verificationRequestId}/files", verificationRequestId)
                                .file(verificationFile)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalFilename").value("career-proof.pdf"))
                .andReturn();

        Long verificationFileId = objectMapper.readTree(verificationFileResult.getResponse().getContentAsString())
                .path("data")
                .path("verificationFileId")
                .asLong();

        mockMvc.perform(get("/api/v1/freelancers/me/verifications/{verificationRequestId}/files", verificationRequestId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].verificationFileId").value(verificationFileId));

        userRepository.save(createUser("admin-epic5@test.com", "ROLE_ADMIN"));
        String adminToken = login("admin-epic5@test.com");

        mockMvc.perform(get("/api/v1/admin/verifications")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));

        mockMvc.perform(patch("/api/v1/admin/verifications/{verificationRequestId}/approve", verificationRequestId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificationReviewRequest("approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/freelancers/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verifiedYn").value(true));
    }

    @Test
    @DisplayName("file upload rejects mismatched or unsupported file types")
    void fileUploadRejectsInvalidType() throws Exception {
        userRepository.save(createUser("file-policy@test.com", "ROLE_USER"));
        String accessToken = login("file-policy@test.com");

        FreelancerProfileUpsertRequest profileRequest = new FreelancerProfileUpsertRequest(
                "career",
                true,
                true,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        );

        mockMvc.perform(post("/api/v1/freelancers/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isCreated());

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "portfolio.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/files")
                        .file(invalidFile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("FILE_400_1"));

        MockMultipartFile mismatchedFile = new MockMultipartFile(
                "file",
                "portfolio.pdf",
                MediaType.TEXT_PLAIN_VALUE,
                "not actually pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/freelancers/me/files")
                        .file(mismatchedFile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("FILE_400_1"));
    }

    private String login(String email) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest(email, "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }

    private User createUser(String email, String roleCode) {
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .name(email)
                .phone("010-0000-0000")
                .intro("intro")
                .roleCode(roleCode)
                .activeYn(true)
                .build();
    }
}
