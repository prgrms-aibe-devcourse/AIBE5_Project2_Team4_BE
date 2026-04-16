package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminDashboardControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerOne = saveUser("freelancer1@test.com", "freelancer1", UserRole.FREELANCER);
        User freelancerTwo = saveUser("freelancer2@test.com", "freelancer2", UserRole.FREELANCER);

        var profileOne = saveFreelancerProfile(freelancerOne, true, true);
        var profileTwo = saveFreelancerProfile(freelancerTwo, false, true);

        Project requestedProject = saveProject(owner, ProjectStatus.REQUESTED);
        Project acceptedProject = saveProject(owner, ProjectStatus.ACCEPTED);
        Project inProgressProject = saveProject(owner, ProjectStatus.IN_PROGRESS);
        Project completedProject = saveProject(owner, ProjectStatus.COMPLETED);
        saveProject(owner, ProjectStatus.CANCELLED);

        saveAcceptedProposal(acceptedProject, profileOne);
        saveAcceptedProposal(inProgressProject, profileOne);
        saveAcceptedProposal(completedProject, profileOne);

        Review review = saveReview(completedProject, 5, true);
        saveReport(review, owner, ReportReasonType.SPAM, ReportStatus.PENDING, null);
        saveVerification(profileTwo, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);
        saveNotice(admin, true);

        mockMvc.perform(get("/api/v1/admin/dashboard").with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(4))
                .andExpect(jsonPath("$.data.totalFreelancers").value(2))
                .andExpect(jsonPath("$.data.verifiedFreelancers").value(1))
                .andExpect(jsonPath("$.data.pendingVerifications").value(1))
                .andExpect(jsonPath("$.data.requestedProjects").value(1))
                .andExpect(jsonPath("$.data.acceptedProjects").value(1))
                .andExpect(jsonPath("$.data.inProgressProjects").value(1))
                .andExpect(jsonPath("$.data.completedProjects").value(1))
                .andExpect(jsonPath("$.data.cancelledProjects").value(1))
                .andExpect(jsonPath("$.data.pendingReports").value(1))
                .andExpect(jsonPath("$.data.blindedReviews").value(1))
                .andExpect(jsonPath("$.data.publishedNotices").value(1))
                .andExpect(jsonPath("$.data.recentPendingVerifications[0].verificationId").exists())
                .andExpect(jsonPath("$.data.recentReports[0].reportId").exists())
                .andExpect(jsonPath("$.data.recentProjects[0].projectId").exists());
    }

    @Test
    void getDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("COMMON_401"));
    }

    @Test
    void getDashboardForbiddenForNonAdmin() throws Exception {
        User user = saveUser("user@test.com", "user", UserRole.USER);

        mockMvc.perform(get("/api/v1/admin/dashboard").with(userPrincipal(user)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }
}
