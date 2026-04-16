package com.ieum.ansimdonghaeng.domain.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
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
class AdminProjectControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProjectsSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        Project requestedProject = saveProject(owner, ProjectStatus.REQUESTED);

        mockMvc.perform(get("/api/v1/admin/projects").with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].projectId").value(requestedProject.getId()))
                .andExpect(jsonPath("$.data.content[0].owner.userId").value(owner.getId()));
    }

    @Test
    void getProjectDetailSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        Project acceptedProject = saveProject(owner, ProjectStatus.ACCEPTED);
        var acceptedProposal = saveAcceptedProposal(acceptedProject, profile);

        mockMvc.perform(get("/api/v1/admin/projects/{projectId}", acceptedProject.getId()).with(adminPrincipal(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(acceptedProject.getId()))
                .andExpect(jsonPath("$.data.owner.userId").value(owner.getId()))
                .andExpect(jsonPath("$.data.acceptedProposal.proposalId").value(acceptedProposal.getId()));
    }

    @Test
    void cancelProjectSuccess() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var profile = saveFreelancerProfile(freelancerUser, true, true);
        Project acceptedProject = saveProject(owner, ProjectStatus.ACCEPTED);
        saveAcceptedProposal(acceptedProject, profile);

        mockMvc.perform(patch("/api/v1/admin/projects/{projectId}/cancel", acceptedProject.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "admin cancel"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelledReason").value("admin cancel"));
    }

    @Test
    void cancelCompletedProjectFails() throws Exception {
        User admin = saveUser("admin@test.com", "admin", UserRole.ADMIN);
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        Project completedProject = saveProject(owner, ProjectStatus.COMPLETED);

        mockMvc.perform(patch("/api/v1/admin/projects/{projectId}/cancel", completedProject.getId())
                        .with(adminPrincipal(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "not allowed"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_4"));
    }
}
