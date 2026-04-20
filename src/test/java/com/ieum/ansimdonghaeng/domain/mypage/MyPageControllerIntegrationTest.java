package com.ieum.ansimdonghaeng.domain.mypage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyPageControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("사용자 마이페이지 요약은 내 프로젝트, 리뷰, 미읽음 알림 집계를 반환한다")
    void getMyPageSummaryForUser() throws Exception {
        User owner = saveUser("mypage-user@test.com", "owner", UserRole.USER);
        Project requestedProject = saveProject(owner, ProjectStatus.REQUESTED);
        Project completedProject = saveProject(owner, ProjectStatus.COMPLETED);
        notificationRepository.saveAndFlush(Notification.reviewRequest(owner, completedProject, "review", "write review"));
        saveReview(completedProject, 5, false);

        mockMvc.perform(get("/api/v1/users/me/mypage").with(userPrincipal(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account.userId").value(owner.getId()))
                .andExpect(jsonPath("$.data.projects.total").value(2))
                .andExpect(jsonPath("$.data.projects.requested").value(1))
                .andExpect(jsonPath("$.data.projects.completed").value(1))
                .andExpect(jsonPath("$.data.reviews.written").value(1))
                .andExpect(jsonPath("$.data.notifications.unread").value(1))
                .andExpect(jsonPath("$.data.freelancer").doesNotExist());
    }

    @Test
    @DisplayName("프리랜서 마이페이지 요약은 제안, 검증, 포트폴리오 집계를 포함한다")
    void getMyPageSummaryForFreelancer() throws Exception {
        User freelancerUser = saveUser("mypage-free@test.com", "freelancer", UserRole.FREELANCER);
        FreelancerProfile profile = saveFreelancerProfile(freelancerUser, true, true);
        User owner = saveUser("proposal-owner@test.com", "owner", UserRole.USER);
        Project project = saveProject(owner, ProjectStatus.REQUESTED);
        proposalRepository.saveAndFlush(com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal.create(project, profile, "pending"));
        saveVerification(profile, com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType.LICENSE,
                com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus.PENDING, null, null);
        freelancerServiceUploadFile(profile);

        mockMvc.perform(get("/api/v1/users/me/mypage").with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account.roleCode").value("ROLE_FREELANCER"))
                .andExpect(jsonPath("$.data.freelancer.freelancerProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.data.freelancer.totalProposals").value(1))
                .andExpect(jsonPath("$.data.freelancer.pendingProposals").value(1))
                .andExpect(jsonPath("$.data.freelancer.totalVerificationRequests").value(1))
                .andExpect(jsonPath("$.data.freelancer.pendingVerificationRequests").value(1))
                .andExpect(jsonPath("$.data.freelancer.portfolioFileCount").value(1));
    }

    @Test
    @DisplayName("내 리뷰 목록은 프리랜서 정보와 신고 여부를 포함해 반환한다")
    void getMyReviews() throws Exception {
        User owner = saveUser("my-reviews-owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("my-reviews-free@test.com", "freelancer", UserRole.FREELANCER);
        FreelancerProfile profile = saveFreelancerProfile(freelancerUser, true, true);
        Project project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, profile);
        Review review = saveReview(project, 4, false);
        User reporter = saveUser("reporter@test.com", "reporter", UserRole.USER);
        saveReport(review, reporter, ReportReasonType.SPAM, ReportStatus.PENDING, null);

        mockMvc.perform(get("/api/v1/users/me/reviews").with(userPrincipal(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data.content[0].freelancerProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.data.content[0].freelancerName").value(freelancerUser.getName()))
                .andExpect(jsonPath("$.data.content[0].reported").value(true))
                .andExpect(jsonPath("$.data.content[0].blinded").value(false));
    }

    private void freelancerServiceUploadFile(FreelancerProfile profile) {
        freelancerFileRepository.saveAndFlush(com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile.create(
                profile,
                "portfolio.pdf",
                "portfolio-1.pdf",
                "https://example.com/portfolio.pdf",
                "application/pdf",
                2048L,
                0
        ));
    }
}
