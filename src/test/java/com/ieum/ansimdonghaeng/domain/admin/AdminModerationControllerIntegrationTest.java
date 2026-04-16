package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
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
class AdminModerationControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getReviewsSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 5, false);

        mockMvc.perform(get("/api/v1/admin/reviews").with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data.content[0].writer.userId").value(owner.getId()));
    }

    @Test
    void getReportsSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 4, false);
        Report report = saveReport(review, owner, ReportReasonType.SPAM, ReportStatus.PENDING, null);

        mockMvc.perform(get("/api/v1/admin/reports").with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reportId").value(report.getId()))
                .andExpect(jsonPath("$.data.content[0].reporter.userId").value(owner.getId()));
    }

    @Test
    void resolveReportSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 4, false);
        Report report = saveReport(review, owner, ReportReasonType.ABUSE, ReportStatus.PENDING, null);

        mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/resolve", report.getId()).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.handledBy.userId").value(admin.getId()));
    }

    @Test
    void rejectReportSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 4, false);
        Report report = saveReport(review, owner, ReportReasonType.ETC, ReportStatus.PENDING, null);

        mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/reject", report.getId()).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.handledBy.userId").value(admin.getId()));
    }

    @Test
    void alreadyHandledReportFails() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 4, false);
        Report report = saveReport(review, owner, ReportReasonType.SPAM, ReportStatus.RESOLVED, admin);

        mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/reject", report.getId()).with(adminPrincipal(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("REPORT_409_1"));
    }

    @Test
    void blindAndUnblindReviewSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 5, false);

        mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/blind", review.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "moderation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blindedYn").value(true));

        mockMvc.perform(patch("/api/v1/admin/reviews/{reviewId}/unblind", review.getId())
                        .with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blindedYn").value(false));
    }
}
