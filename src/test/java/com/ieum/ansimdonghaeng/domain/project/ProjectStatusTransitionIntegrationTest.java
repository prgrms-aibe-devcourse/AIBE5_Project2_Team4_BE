package com.ieum.ansimdonghaeng.domain.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
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
class ProjectStatusTransitionIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void assignedFreelancerCanStartAndCompleteProject() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        freelancerProfile.updateStats(java.math.BigDecimal.ZERO, 0L);
        freelancerProfileRepository.saveAndFlush(freelancerProfile);
        var project = saveProject(owner, ProjectStatus.REQUESTED);
        saveAcceptedProposal(project, freelancerProfile);
        project.accept(LocalDateTime.of(2026, 4, 18, 10, 0));
        projectRepository.saveAndFlush(project);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/start", project.getId()).with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/v1/projects/{projectId}/complete", project.getId()).with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        var refreshedProfile = freelancerProfileRepository.findById(freelancerProfile.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(refreshedProfile.getActivityCount()).isEqualTo(1L);
    }

    @Test
    void projectCannotStartBeforeProposalAccept() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.REQUESTED);
        proposalRepository.saveAndFlush(com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal.create(project, freelancerProfile, "hello"));

        mockMvc.perform(patch("/api/v1/projects/{projectId}/start", project.getId()).with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_1"));
    }
}
