package com.ieum.ansimdonghaeng.oracle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
@EnabledIfSystemProperty(named = "oracle.smoke", matches = "true")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "app.file-storage.base-dir=./target/oracle-smoke-storage"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OracleLocalSmokeTest {

    private static final String PASSWORD = "password123!";
    private static final Path SMOKE_STORAGE_DIR = Path.of("./target/oracle-smoke-storage").toAbsolutePath().normalize();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterAll
    void cleanStorage() throws IOException {
        if (!Files.exists(SMOKE_STORAGE_DIR)) {
            return;
        }

        try (var paths = Files.walk(SMOKE_STORAGE_DIR)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Smoke storage cleanup should not fail the suite after assertions completed.
                        }
                    });
        }
    }

    @Test
    void localOracleSchemaAndMajorApiFlowsWork() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());

        assertColumnType("APP_USER", "ACTIVE_YN", "CHAR");
        assertColumnType("PROJECT", "REQUEST_DETAIL", "CLOB");
        assertColumnType("NOTICE", "PUBLISHED_YN", "CHAR");
        assertColumnType("REFRESH_TOKEN", "TOKEN_VALUE", "VARCHAR2");
        assertSequenceExists("SEQ_APP_USER");
        assertSequenceExists("SEQ_PROJECT");
        assertSequenceExists("SEQ_REFRESH_TOKEN");

        ensureReferenceCodeData();

        List<JsonNode> projectTypes = getCodeList("/api/v1/codes/project-types");
        List<JsonNode> regions = getCodeList("/api/v1/codes/regions");
        List<JsonNode> timeSlots = getCodeList("/api/v1/codes/available-time-slots");
        List<JsonNode> reviewTags = getCodeList("/api/v1/reviews/tag-codes");

        assertThat(projectTypes).isNotEmpty();
        assertThat(regions).isNotEmpty();
        assertThat(timeSlots).isNotEmpty();
        assertThat(reviewTags).isNotEmpty();

        String projectTypeCode = projectTypes.getFirst().path("code").asText();
        String regionCode = regions.getFirst().path("code").asText();
        String timeSlotCode = timeSlots.getFirst().path("code").asText();
        List<String> reviewTagCodes = reviewTags.stream()
                .limit(2)
                .map(tag -> tag.path("code").asText())
                .toList();

        String ownerEmail = "oracle-owner-" + suffix + "@test.com";
        String freelancerEmail = "oracle-freelancer-" + suffix + "@test.com";
        String reporterEmail = "oracle-reporter-" + suffix + "@test.com";
        String adminEmail = "oracle-admin-" + suffix + "@test.com";

        signup(ownerEmail, "oracle-owner-" + suffix);
        signup(freelancerEmail, "oracle-freelancer-" + suffix);
        signup(reporterEmail, "oracle-reporter-" + suffix);
        createAdmin(adminEmail, "oracle-admin-" + suffix);

        TokenBundle ownerTokens = login(ownerEmail);
        TokenBundle freelancerTokens = login(freelancerEmail);
        TokenBundle reporterTokens = login(reporterEmail);
        TokenBundle adminTokens = login(adminEmail);

        JsonNode refreshedOwnerTokens = dataFrom(postJson(
                "/api/v1/auth/refresh",
                Map.of("refreshToken", ownerTokens.refreshToken()),
                null
        ));
        assertThat(refreshedOwnerTokens.path("accessToken").asText()).isNotBlank();

        JsonNode ownerProfile = dataFrom(getAuthorized("/api/v1/users/me", ownerTokens.accessToken()));
        assertThat(ownerProfile.path("email").asText()).isEqualTo(ownerEmail);

        JsonNode ownerMyPage = dataFrom(getAuthorized("/api/v1/users/me/mypage", ownerTokens.accessToken()));
        assertThat(ownerMyPage.path("user").path("email").asText()).isEqualTo(ownerEmail);

        JsonNode freelancerProfile = dataFrom(postJson(
                "/api/v1/freelancers/me/profile",
                Map.of(
                        "careerDescription", "oracle-career-" + suffix,
                        "caregiverYn", true,
                        "publicYn", true,
                        "activityRegionCodes", List.of(regionCode),
                        "availableTimeSlotCodes", List.of(timeSlotCode),
                        "projectTypeCodes", List.of(projectTypeCode)
                ),
                freelancerTokens.accessToken()
        ));
        Long freelancerProfileId = freelancerProfile.path("freelancerProfileId").asLong();

        JsonNode publicFreelancerDetail = dataFrom(getPublic("/api/v1/freelancers/{freelancerProfileId}", freelancerProfileId));
        assertThat(publicFreelancerDetail.path("freelancerProfileId").asLong()).isEqualTo(freelancerProfileId);

        JsonNode portfolioFile = dataFrom(multipartAuthorized(
                "/api/v1/freelancers/me/files",
                "file",
                "portfolio.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "oracle-portfolio".getBytes(),
                freelancerTokens.accessToken()
        ));
        String portfolioViewUrl = portfolioFile.path("viewUrl").asText();
        String portfolioDownloadUrl = portfolioFile.path("downloadUrl").asText();

        mockMvc.perform(get(portfolioViewUrl))
                .andExpect(status().isOk());
        mockMvc.perform(get(portfolioDownloadUrl)
                        .header(HttpHeaders.AUTHORIZATION, bearer(freelancerTokens.accessToken())))
                .andExpect(status().isOk());

        JsonNode verification = dataFrom(postJson(
                "/api/v1/freelancers/me/verifications",
                Map.of(
                        "type", "LICENSE",
                        "requestMessage", "oracle-verification-" + suffix
                ),
                freelancerTokens.accessToken()
        ));
        Long verificationId = verification.path("verificationId").asLong();

        JsonNode verificationFile = dataFrom(multipartAuthorized(
                "/api/v1/freelancers/me/verifications/" + verificationId + "/files",
                "file",
                "proof.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-oracle-proof".getBytes(),
                freelancerTokens.accessToken()
        ));
        String verificationDownloadUrl = verificationFile.path("downloadUrl").asText();

        dataFrom(getAuthorized("/api/v1/freelancers/me/verifications/" + verificationId, freelancerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/freelancers/me/verifications/" + verificationId + "/files", freelancerTokens.accessToken()));
        mockMvc.perform(get(verificationDownloadUrl)
                        .header(HttpHeaders.AUTHORIZATION, bearer(freelancerTokens.accessToken())))
                .andExpect(status().isOk());

        JsonNode project = dataFrom(postJson(
                "/api/v1/projects",
                Map.of(
                        "title", "oracle-project-" + suffix,
                        "projectTypeCode", projectTypeCode,
                        "serviceRegionCode", regionCode,
                        "requestedStartAt", "2026-05-01T09:00:00",
                        "requestedEndAt", "2026-05-01T12:00:00",
                        "serviceAddress", "Seoul address " + suffix,
                        "serviceDetailAddress", "Building " + suffix,
                        "requestDetail", "oracle-request-detail-" + suffix
                ),
                ownerTokens.accessToken()
        ));
        Long projectId = project.path("projectId").asLong();

        JsonNode proposal = dataFrom(postJson(
                "/api/v1/projects/" + projectId + "/proposals",
                Map.of(
                        "freelancerProfileId", freelancerProfileId,
                        "message", "oracle-proposal-" + suffix
                ),
                ownerTokens.accessToken()
        ));
        Long proposalId = proposal.path("proposalId").asLong();

        dataFrom(getAuthorized("/api/v1/projects/me", ownerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/projects/" + projectId, ownerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/projects/" + projectId + "/proposals", ownerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/freelancers/me/proposals", freelancerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/freelancers/me/proposals/" + proposalId, freelancerTokens.accessToken()));

        JsonNode acceptedProposal = dataFrom(patchJson(
                "/api/v1/freelancers/me/proposals/" + proposalId + "/accept",
                null,
                freelancerTokens.accessToken()
        ));
        assertThat(acceptedProposal.path("proposalStatus").asText()).isEqualTo("ACCEPTED");

        JsonNode rejectedProposal = createAndRejectProposal(ownerTokens.accessToken(), freelancerTokens.accessToken(), suffix, projectTypeCode, regionCode, freelancerProfileId);
        assertThat(rejectedProposal.path("proposalStatus").asText()).isEqualTo("REJECTED");

        JsonNode startedProject = dataFrom(patchJson(
                "/api/v1/projects/" + projectId + "/start",
                null,
                freelancerTokens.accessToken()
        ));
        assertThat(startedProject.path("status").asText()).isEqualTo("IN_PROGRESS");

        JsonNode completedProject = dataFrom(patchJson(
                "/api/v1/projects/" + projectId + "/complete",
                null,
                freelancerTokens.accessToken()
        ));
        assertThat(completedProject.path("status").asText()).isEqualTo("COMPLETED");

        JsonNode review = dataFrom(postJson(
                "/api/v1/projects/" + projectId + "/reviews",
                Map.of(
                        "rating", 5,
                        "tagCodes", reviewTagCodes,
                        "content", "oracle-review-" + suffix
                ),
                ownerTokens.accessToken()
        ));
        Long reviewId = review.path("reviewId").asLong();

        JsonNode updatedReview = dataFrom(patchJson(
                "/api/v1/users/me/reviews/" + reviewId,
                Map.of(
                        "rating", 4,
                        "tagCodes", reviewTagCodes.subList(0, 1),
                        "content", "oracle-review-updated-" + suffix
                ),
                ownerTokens.accessToken()
        ));
        assertThat(updatedReview.path("rating").asInt()).isEqualTo(4);

        dataFrom(getAuthorized("/api/v1/users/me/reviews", ownerTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/users/me/reviews/" + reviewId, ownerTokens.accessToken()));
        dataFrom(getPublic("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfileId));

        JsonNode report = dataFrom(postJson(
                "/api/v1/reviews/" + reviewId + "/reports",
                Map.of(
                        "reasonType", "SPAM",
                        "reasonDetail", "oracle-report-" + suffix
                ),
                reporterTokens.accessToken()
        ));
        Long reportId = report.path("reportId").asLong();

        dataFrom(getAuthorized("/api/v1/reports/me", reporterTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/notifications", ownerTokens.accessToken()));

        JsonNode adminVerificationList = dataFrom(getAuthorized(
                "/api/v1/admin/verifications?keyword=" + freelancerEmail,
                adminTokens.accessToken()
        ));
        assertThat(adminVerificationList.path("content").isArray()).isTrue();
        dataFrom(getAuthorized("/api/v1/admin/verifications/" + verificationId, adminTokens.accessToken()));

        JsonNode approvedVerification = dataFrom(patchJson(
                "/api/v1/admin/verifications/" + verificationId + "/approve",
                Map.of("comment", "oracle-approved-" + suffix),
                adminTokens.accessToken()
        ));
        assertThat(approvedVerification.path("status").asText()).isEqualTo("APPROVED");

        dataFrom(getAuthorized(
                "/api/v1/admin/projects?keyword=oracle-project-" + suffix + "&writerKeyword=" + ownerEmail,
                adminTokens.accessToken()
        ));
        dataFrom(getAuthorized("/api/v1/admin/projects/" + projectId, adminTokens.accessToken()));

        dataFrom(getAuthorized(
                "/api/v1/admin/freelancers?keyword=" + freelancerEmail + "&region=" + regionCode + "&projectType=" + projectTypeCode,
                adminTokens.accessToken()
        ));
        dataFrom(getAuthorized("/api/v1/admin/freelancers/" + freelancerProfileId, adminTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/admin/reviews", adminTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/admin/reports", adminTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/admin/reports/" + reportId, adminTokens.accessToken()));
        dataFrom(patchJson("/api/v1/admin/reports/" + reportId + "/resolve", null, adminTokens.accessToken()));
        dataFrom(getAuthorized("/api/v1/admin/dashboard", adminTokens.accessToken()));

        JsonNode adminNotice = dataFrom(postJson(
                "/api/v1/admin/notices",
                Map.of(
                        "title", "oracle-notice-" + suffix,
                        "content", "oracle-notice-content-" + suffix,
                        "publishNow", true
                ),
                adminTokens.accessToken()
        ));
        Long noticeId = adminNotice.path("noticeId").asLong();
        dataFrom(getPublic("/api/v1/notices"));
        dataFrom(getPublic("/api/v1/notices/{noticeId}", noticeId));

        JsonNode ownerNotifications = dataFrom(getAuthorized("/api/v1/notifications", ownerTokens.accessToken()));
        assertThat(ownerNotifications.path("content").isArray()).isTrue();
        if (ownerNotifications.path("content").size() > 0) {
            long notificationId = ownerNotifications.path("content").get(0).path("notificationId").asLong();
            dataFrom(patchJson("/api/v1/notifications/" + notificationId + "/read", null, ownerTokens.accessToken()));
        }
        dataFrom(patchJson("/api/v1/notifications/read-all", null, ownerTokens.accessToken()));

        JsonNode freelancerMyPage = dataFrom(getAuthorized("/api/v1/users/me/mypage", freelancerTokens.accessToken()));
        assertThat(freelancerMyPage.path("verificationSummary").path("status").asText()).isEqualTo("APPROVED");

        JsonNode logoutResponse = dataFrom(postJson("/api/v1/auth/logout", null, ownerTokens.accessToken()));
        assertThat(logoutResponse.path("revokedRefreshTokenCount").asLong()).isGreaterThanOrEqualTo(1L);
    }

    private JsonNode createAndRejectProposal(String ownerAccessToken,
                                             String freelancerAccessToken,
                                             String suffix,
                                             String projectTypeCode,
                                             String regionCode,
                                             Long freelancerProfileId) throws Exception {
        JsonNode secondProject = dataFrom(postJson(
                "/api/v1/projects",
                Map.of(
                        "title", "oracle-reject-project-" + suffix,
                        "projectTypeCode", projectTypeCode,
                        "serviceRegionCode", regionCode,
                        "requestedStartAt", "2026-05-02T09:00:00",
                        "requestedEndAt", "2026-05-02T12:00:00",
                        "serviceAddress", "Seoul reject address " + suffix,
                        "serviceDetailAddress", "Reject building " + suffix,
                        "requestDetail", "oracle-reject-request-detail-" + suffix
                ),
                ownerAccessToken
        ));
        long secondProjectId = secondProject.path("projectId").asLong();

        JsonNode secondProposal = dataFrom(postJson(
                "/api/v1/projects/" + secondProjectId + "/proposals",
                Map.of(
                        "freelancerProfileId", freelancerProfileId,
                        "message", "oracle-reject-proposal-" + suffix
                ),
                ownerAccessToken
        ));
        long secondProposalId = secondProposal.path("proposalId").asLong();

        return dataFrom(patchJson(
                "/api/v1/freelancers/me/proposals/" + secondProposalId + "/reject",
                null,
                freelancerAccessToken
        ));
    }

    private void signup(String email, String name) throws Exception {
        postJson(
                "/api/v1/auth/signup",
                Map.of(
                        "email", email,
                        "password", PASSWORD,
                        "name", name,
                        "phone", "010-0000-0000",
                        "intro", "oracle-smoke"
                ),
                null
        );
    }

    private void createAdmin(String email, String name) {
        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .name(name)
                .phone("010-9999-9999")
                .intro("oracle-admin")
                .roleCode(UserRole.ADMIN.getCode())
                .activeYn(true)
                .build());
    }

    private TokenBundle login(String email) throws Exception {
        JsonNode data = dataFrom(postJson(
                "/api/v1/auth/login",
                Map.of(
                        "email", email,
                        "password", PASSWORD
                ),
                null
        ));
        return new TokenBundle(
                data.path("accessToken").asText(),
                data.path("refreshToken").asText()
        );
    }

    private List<JsonNode> getCodeList(String path) throws Exception {
        JsonNode data = dataFrom(getPublic(path));
        List<JsonNode> codes = new ArrayList<>();
        data.forEach(codes::add);
        return codes;
    }

    private void assertColumnType(String tableName, String columnName, String expectedDataType) {
        String dataType = jdbcTemplate.queryForObject(
                "select data_type from user_tab_columns where table_name = ? and column_name = ?",
                String.class,
                tableName,
                columnName
        );
        assertThat(dataType).isEqualTo(expectedDataType);
    }

    private void assertSequenceExists(String sequenceName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from user_sequences where sequence_name = ?",
                Integer.class,
                sequenceName
        );
        assertThat(count).isNotNull();
        assertThat(count).isPositive();
    }

    private void ensureReferenceCodeData() {
        jdbcTemplate.update("""
                insert into PROJECT_TYPE_CODE (PROJECT_TYPE_CODE, PROJECT_TYPE_NAME, SORT_ORDER, ACTIVE_YN)
                select ?, ?, ?, ?
                from dual
                where not exists (
                    select 1
                    from PROJECT_TYPE_CODE
                    where PROJECT_TYPE_CODE = ?
                )
                """, "SMOKE_PROJECT_TYPE", "Smoke Project Type", 9998, "Y", "SMOKE_PROJECT_TYPE");

        jdbcTemplate.update("""
                insert into REGION_CODE (REGION_CODE, PARENT_REGION_CODE, REGION_NAME, REGION_LEVEL, ACTIVE_YN)
                select ?, ?, ?, ?, ?
                from dual
                where not exists (
                    select 1
                    from REGION_CODE
                    where REGION_CODE = ?
                )
                """, "SMOKE_REGION", null, "Smoke Region", 1, "Y", "SMOKE_REGION");

        jdbcTemplate.update("""
                insert into AVAILABLE_TIME_SLOT_CODE (TIME_SLOT_CODE, TIME_SLOT_NAME, START_MINUTE, END_MINUTE, SORT_ORDER, ACTIVE_YN)
                select ?, ?, ?, ?, ?, ?
                from dual
                where not exists (
                    select 1
                    from AVAILABLE_TIME_SLOT_CODE
                    where TIME_SLOT_CODE = ?
                )
                """, "SMOKE_TIME_SLOT", "Smoke Time Slot", 540, 720, 9998, "Y", "SMOKE_TIME_SLOT");

        jdbcTemplate.update("""
                insert into REVIEW_TAG_CODE (REVIEW_TAG_CODE, REVIEW_TAG_NAME, SORT_ORDER, ACTIVE_YN)
                select ?, ?, ?, ?
                from dual
                where not exists (
                    select 1
                    from REVIEW_TAG_CODE
                    where REVIEW_TAG_CODE = ?
                )
                """, "SMOKE_TAG", "Smoke Tag", 9998, "Y", "SMOKE_TAG");
    }

    private JsonNode dataFrom(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.path("success").asBoolean()).isTrue();
        return root.path("data");
    }

    private MvcResult getAuthorized(String path, String accessToken) throws Exception {
        return mockMvc.perform(get(path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult getPublic(String path, Object... uriVariables) throws Exception {
        return mockMvc.perform(get(path, uriVariables))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult postJson(String path, Object body, String accessToken) throws Exception {
        var builder = post(path)
                .contentType(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            builder.header(HttpHeaders.AUTHORIZATION, bearer(accessToken));
        }
        if (body != null) {
            builder.content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(builder)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    private MvcResult patchJson(String path, Object body, String accessToken) throws Exception {
        var builder = patch(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken));
        if (body != null) {
            builder.content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult multipartAuthorized(String path,
                                          String partName,
                                          String filename,
                                          String contentType,
                                          byte[] content,
                                          String accessToken) throws Exception {
        return mockMvc.perform(multipart(path)
                        .file(new MockMultipartFile(partName, filename, contentType, content))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record TokenBundle(String accessToken, String refreshToken) {
    }
}
