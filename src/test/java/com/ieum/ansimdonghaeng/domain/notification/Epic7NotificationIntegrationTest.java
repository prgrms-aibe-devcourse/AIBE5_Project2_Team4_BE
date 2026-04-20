package com.ieum.ansimdonghaeng.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.notification.entity.NotificationType;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
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
class Epic7NotificationIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("제안 생성과 수락, 프로젝트 상태 변경 시 알림이 생성된다")
    void proposalAndProjectLifecycleCreateNotifications() throws Exception {
        User owner = saveUser("owner-noti@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer-noti@test.com", "freelancer", UserRole.FREELANCER);
        FreelancerProfile freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        Project project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "freelancerProfileId", freelancerProfile.getId(),
                                "message", "proposal message"
                        ))))
                .andExpect(status().isCreated());

        var proposal = proposalRepository.findAll().get(0);
        assertThat(notificationRepository.findAll())
                .extracting(notification -> notification.getNotificationType())
                .contains(NotificationType.PROPOSAL_RECEIVED);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", proposal.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/projects/{projectId}/start", project.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/projects/{projectId}/complete", project.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk());

        assertThat(notificationRepository.findAll())
                .extracting(notification -> notification.getNotificationType())
                .contains(NotificationType.PROPOSAL_ACCEPTED)
                .contains(NotificationType.PROJECT_STATUS_CHANGED)
                .contains(NotificationType.REVIEW_REQUEST);
    }
}
