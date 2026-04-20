package com.ieum.ansimdonghaeng.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.notification.entity.NotificationType;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserMyPageIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMyPageAggregatesProjectReviewNotificationAndFreelancerData() throws Exception {
        User user = saveUser("mypage@test.com", "mypage-user", UserRole.FREELANCER);
        User otherOwner = saveUser("owner@test.com", "owner", UserRole.USER);
        var freelancerProfile = saveFreelancerProfile(user, false, true);

        saveProject(user, ProjectStatus.REQUESTED);
        Project completedProject = saveProject(user, ProjectStatus.COMPLETED);
        saveProject(user, ProjectStatus.CANCELLED);

        Review review = saveReview(completedProject, 5, false);
        saveReport(review, user, ReportReasonType.SPAM, ReportStatus.PENDING, null);
        notificationRepository.save(Notification.create(
                user,
                NotificationType.NOTICE,
                "notice",
                "content",
                null,
                null,
                null,
                null,
                null
        ));
        saveVerification(freelancerProfile, VerificationType.LICENSE, VerificationStatus.PENDING, null, null);

        Proposal pendingProposal = Proposal.create(saveProject(otherOwner, ProjectStatus.REQUESTED), freelancerProfile, "pending");
        proposalRepository.saveAndFlush(pendingProposal);
        saveAcceptedProposal(saveProject(otherOwner, ProjectStatus.REQUESTED), freelancerProfile);

        mockMvc.perform(get("/api/v1/users/me/mypage").with(freelancerPrincipal(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.projectStats.totalProjects").value(3))
                .andExpect(jsonPath("$.data.projectStats.requestedProjects").value(1))
                .andExpect(jsonPath("$.data.projectStats.completedProjects").value(1))
                .andExpect(jsonPath("$.data.projectStats.cancelledProjects").value(1))
                .andExpect(jsonPath("$.data.reviewStats.writtenReviewCount").value(1))
                .andExpect(jsonPath("$.data.reviewStats.reportedCount").value(1))
                .andExpect(jsonPath("$.data.notificationSummary.unreadNotificationCount").value(1))
                .andExpect(jsonPath("$.data.freelancerProfile.freelancerProfileId").value(freelancerProfile.getId()))
                .andExpect(jsonPath("$.data.verificationSummary.status").value("PENDING"))
                .andExpect(jsonPath("$.data.proposalSummary.receivedProposalCount").value(2))
                .andExpect(jsonPath("$.data.proposalSummary.pendingReceivedProposalCount").value(1));
    }
}
