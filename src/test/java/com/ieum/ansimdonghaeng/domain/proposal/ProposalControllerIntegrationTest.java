package com.ieum.ansimdonghaeng.domain.proposal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProposalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("user can create proposal for own project")
    void createProposalSuccess() throws Exception {
        User owner = userRepository.save(createUser("owner@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "freelancerProfileId", freelancerProfile.getId(),
                                "message", "proposal message"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(project.getId()))
                .andExpect(jsonPath("$.data.freelancerProfileId").value(freelancerProfile.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("freelancerProfileId missing returns validation 400")
    void createProposalFailsWhenFreelancerProfileIdMissing() throws Exception {
        User owner = userRepository.save(createUser("owner-missing-profile@test.com", "owner", "ROLE_USER"));
        Project project = projectRepository.save(createProject(owner));

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "message", "freelancer id missing"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"))
                .andExpect(jsonPath("$.error.message").value("freelancerProfileId: freelancerProfileId is required"));
    }

    @Test
    @DisplayName("duplicate proposal for same project and freelancer returns conflict")
    void createProposalFailsWhenDuplicate() throws Exception {
        User owner = userRepository.save(createUser("owner-dup@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-dup@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "existing proposal"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "freelancerProfileId", freelancerProfile.getId(),
                                "message", "duplicate proposal"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_409_1"));
    }

    @Test
    @DisplayName("freelancer can list own proposals with status filter")
    void getMyProposalsWithStatusFilter() throws Exception {
        User owner = userRepository.save(createUser("owner-list@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-list@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        Proposal pendingProposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "pending"));
        Proposal rejectedProposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "rejected"));
        rejectedProposal.reject(LocalDateTime.of(2026, 4, 15, 10, 0));
        proposalRepository.saveAndFlush(rejectedProposal);

        mockMvc.perform(get("/api/v1/freelancers/me/proposals")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].proposalId").value(pendingProposal.getId()))
                .andExpect(jsonPath("$.data.content[0].proposalStatus").value("PENDING"));
    }

    @Test
    @DisplayName("other freelancer cannot access proposal detail")
    void getMyProposalFailsForOtherFreelancer() throws Exception {
        User owner = userRepository.save(createUser("owner-detail@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-detail@test.com", "freelancer", "ROLE_FREELANCER"));
        User otherFreelancerUser = userRepository.save(createUser("freelancer-other@test.com", "other-freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        freelancerProfileRepository.save(createProfile(otherFreelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "detail"));

        mockMvc.perform(get("/api/v1/freelancers/me/proposals/{proposalId}", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(otherFreelancerUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_403_1"));
    }

    @Test
    @DisplayName("accepting proposal updates project and rejects other pending proposals")
    void acceptProposalSuccessChangesProjectStatus() throws Exception {
        User owner = userRepository.save(createUser("owner-accept@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-accept@test.com", "accepted-freelancer", "ROLE_FREELANCER"));
        User otherFreelancerUser = userRepository.save(createUser("freelancer-reject@test.com", "other-freelancer", "ROLE_FREELANCER"));
        FreelancerProfile acceptedFreelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        FreelancerProfile rejectedFreelancerProfile = freelancerProfileRepository.save(createProfile(otherFreelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        Proposal acceptedProposal = proposalRepository.saveAndFlush(Proposal.create(project, acceptedFreelancerProfile, "accept me"));
        Proposal rejectedProposal = proposalRepository.saveAndFlush(Proposal.create(project, rejectedFreelancerProfile, "reject me"));

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", acceptedProposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.proposalStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.projectStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.projectAcceptedAt").exists());

        Proposal savedAcceptedProposal = proposalRepository.findById(acceptedProposal.getId()).orElseThrow();
        Proposal savedRejectedProposal = proposalRepository.findById(rejectedProposal.getId()).orElseThrow();
        Project savedProject = projectRepository.findById(project.getId()).orElseThrow();

        Assertions.assertEquals(ProposalStatus.ACCEPTED, savedAcceptedProposal.getStatus());
        Assertions.assertEquals(ProposalStatus.REJECTED, savedRejectedProposal.getStatus());
        Assertions.assertEquals(com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.ACCEPTED, savedProject.getStatus());
        Assertions.assertNotNull(savedProject.getAcceptedAt());
    }

    @Test
    @DisplayName("cannot accept proposal when status is not pending")
    void acceptProposalFailsWhenProposalStatusIsNotPending() throws Exception {
        User owner = userRepository.save(createUser("owner-proposal-status@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-proposal-status@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "already rejected"));
        proposal.reject(LocalDateTime.of(2026, 4, 15, 10, 0));
        proposalRepository.saveAndFlush(proposal);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_400_1"));
    }

    @Test
    @DisplayName("cannot accept proposal when project status is not requested")
    void acceptProposalFailsWhenProjectStatusIsNotRequested() throws Exception {
        User owner = userRepository.save(createUser("owner-project-status@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-project-status@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "accept attempt"));
        project.accept(LocalDateTime.of(2026, 4, 15, 10, 0));
        projectRepository.saveAndFlush(project);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_1"));
    }

    @Test
    @DisplayName("freelancer can explicitly reject pending proposal")
    void rejectProposalSuccess() throws Exception {
        User owner = userRepository.save(createUser("owner-reject@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-reject-self@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "reject target"));

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/reject", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.proposalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.projectStatus").value("REQUESTED"))
                .andExpect(jsonPath("$.data.respondedAt").exists());

        Proposal savedProposal = proposalRepository.findById(proposal.getId()).orElseThrow();
        Assertions.assertEquals(ProposalStatus.REJECTED, savedProposal.getStatus());
        Assertions.assertNotNull(savedProposal.getRespondedAt());
    }

    @Test
    @DisplayName("cannot reject proposal when status is not pending")
    void rejectProposalFailsWhenProposalStatusIsNotPending() throws Exception {
        User owner = userRepository.save(createUser("owner-reject-invalid@test.com", "owner", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-reject-invalid@test.com", "freelancer", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "already rejected"));
        proposal.reject(LocalDateTime.of(2026, 4, 15, 10, 0));
        proposalRepository.saveAndFlush(proposal);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/reject", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_400_1"));
    }

    private User createUser(String email, String name, String roleCode) {
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("1234"))
                .name(name)
                .phone("010-0000-0000")
                .intro("intro")
                .roleCode(roleCode)
                .activeYn(true)
                .build();
    }

    private FreelancerProfile createProfile(User user, boolean publicYn) {
        return FreelancerProfile.create(
                user,
                "career description",
                true,
                true,
                new BigDecimal("4.50"),
                8L,
                publicYn,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        );
    }

    private Project createProject(User owner) {
        return Project.create(
                owner.getId(),
                "hospital companion request",
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 14, 0),
                LocalDateTime.of(2026, 4, 20, 17, 0),
                "123 Teheran-ro, Gangnam-gu, Seoul",
                "3rd floor desk",
                "Need hospital reception and companion service."
        );
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                List.of(new SimpleGrantedAuthority(user.getRoleCode()))
        );
    }
}
