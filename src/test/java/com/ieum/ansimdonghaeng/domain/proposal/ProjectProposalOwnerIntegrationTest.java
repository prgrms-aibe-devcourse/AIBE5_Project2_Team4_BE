package com.ieum.ansimdonghaeng.domain.proposal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
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
class ProjectProposalOwnerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ownerCanListProjectProposals() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);
        proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "hello"));

        mockMvc.perform(get("/api/v1/projects/{projectId}/proposals", project.getId()).with(userPrincipal(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].proposalId").exists())
                .andExpect(jsonPath("$.data.content[0].projectId").value(project.getId()))
                .andExpect(jsonPath("$.data.content[0].freelancerProfileId").value(freelancerProfile.getId()))
                .andExpect(jsonPath("$.data.content[0].freelancer.name").value(freelancerUser.getName()))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    void otherUserCannotListProjectProposals() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User otherUser = saveUser("other@test.com", "other", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);
        proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "hello"));

        mockMvc.perform(get("/api/v1/projects/{projectId}/proposals", project.getId()).with(userPrincipal(otherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("PROJECT_403_1"));
    }
}
