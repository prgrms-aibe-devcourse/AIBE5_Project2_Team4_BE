package com.ieum.ansimdonghaeng.domain.report.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMyReportsReturnsOnlyCurrentUserReports() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User reporter = saveUser("reporter@test.com", "reporter", UserRole.USER);
        User otherReporter = saveUser("other@test.com", "other", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, freelancerProfile);
        Review review = saveReview(project, 4, false);
        saveReport(review, reporter, ReportReasonType.SPAM, ReportStatus.PENDING, null);
        saveReport(review, otherReporter, ReportReasonType.ABUSE, ReportStatus.PENDING, null);

        mockMvc.perform(get("/api/v1/reports/me").with(userPrincipal(reporter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reasonType").value("SPAM"))
                .andExpect(jsonPath("$.data.content[0].review.projectTitle").value(project.getTitle()));
    }
}
