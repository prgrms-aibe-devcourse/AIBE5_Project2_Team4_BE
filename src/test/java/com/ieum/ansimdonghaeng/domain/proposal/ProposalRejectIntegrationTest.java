package com.ieum.ansimdonghaeng.domain.proposal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProposalRejectIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void freelancerCanRejectPendingProposal() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "hello"));

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/reject", proposal.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proposalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.respondedAt").exists());

        Proposal savedProposal = proposalRepository.findById(proposal.getId()).orElseThrow();
        Assertions.assertEquals(com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.REJECTED, savedProposal.getStatus());
        Assertions.assertNotNull(savedProposal.getRespondedAt());
    }

    @Test
    void rejectFailsWhenProposalAlreadyHandled() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "hello"));
        proposal.accept(LocalDateTime.of(2026, 4, 18, 10, 0));
        proposalRepository.saveAndFlush(proposal);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/reject", proposal.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_400_1"));
    }

    @Test
    void deprecatedRejectEndpointIsRemoved() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.REQUESTED);
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "hello"));

        mockMvc.perform(patch("/api/v1/proposals/{proposalId}/reject", proposal.getId())
                        .with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isNotFound());
    }
}
