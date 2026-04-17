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
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자는 본인 프로젝트에 프리랜서 제안을 생성할 수 있다")
    void createProposalSuccess() throws Exception {
        User owner = userRepository.save(createUser("owner@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer@test.com", "프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "freelancerProfileId", freelancerProfile.getId(),
                                "message", "병원 동행이 가능하신지 제안드립니다."
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
    @DisplayName("같은 프로젝트와 프리랜서 조합으로 중복 제안을 만들 수 없다")
    void createProposalFailsWhenDuplicate() throws Exception {
        User owner = userRepository.save(createUser("owner-dup@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-dup@test.com", "프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "기존 제안"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/proposals", project.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "freelancerProfileId", freelancerProfile.getId(),
                                "message", "중복 제안"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_409_1"));
    }

    @Test
    @DisplayName("프리랜서는 자신에게 온 제안 목록을 상태 조건과 함께 조회할 수 있다")
    void getMyProposalsWithStatusFilter() throws Exception {
        User owner = userRepository.save(createUser("owner-list@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-list@test.com", "프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        Proposal pendingProposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "대기 제안"));
        Proposal rejectedProposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "거절 제안"));
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
    @DisplayName("다른 프리랜서는 제안 상세를 볼 수 없다")
    void getMyProposalFailsForOtherFreelancer() throws Exception {
        User owner = userRepository.save(createUser("owner-detail@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-detail@test.com", "프리랜서", "ROLE_FREELANCER"));
        User otherFreelancerUser = userRepository.save(createUser("freelancer-other@test.com", "다른 프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        freelancerProfileRepository.save(createProfile(otherFreelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "제안 상세"));

        mockMvc.perform(get("/api/v1/freelancers/me/proposals/{proposalId}", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(otherFreelancerUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_403_1"));
    }

    @Test
    @DisplayName("프리랜서가 제안을 수락하면 프로젝트 상태가 ACCEPTED로 바뀌고 다른 대기 제안은 거절된다")
    void acceptProposalSuccessChangesProjectStatus() throws Exception {
        User owner = userRepository.save(createUser("owner-accept@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-accept@test.com", "수락 프리랜서", "ROLE_FREELANCER"));
        User otherFreelancerUser = userRepository.save(createUser("freelancer-reject@test.com", "다른 프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile acceptedFreelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        FreelancerProfile rejectedFreelancerProfile = freelancerProfileRepository.save(createProfile(otherFreelancerUser, true));
        Project project = projectRepository.save(createProject(owner));

        Proposal acceptedProposal = proposalRepository.saveAndFlush(Proposal.create(project, acceptedFreelancerProfile, "수락 대상"));
        Proposal rejectedProposal = proposalRepository.saveAndFlush(Proposal.create(project, rejectedFreelancerProfile, "다른 제안"));

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

        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.ACCEPTED, savedAcceptedProposal.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(ProposalStatus.REJECTED, savedRejectedProposal.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus.ACCEPTED, savedProject.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(savedProject.getAcceptedAt());
    }

    @Test
    @DisplayName("이미 응답된 제안은 다시 수락할 수 없다")
    void acceptProposalFailsWhenProposalStatusIsNotPending() throws Exception {
        User owner = userRepository.save(createUser("owner-proposal-status@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-proposal-status@test.com", "프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "이미 거절됨"));
        proposal.reject(LocalDateTime.of(2026, 4, 15, 10, 0));
        proposalRepository.saveAndFlush(proposal);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROPOSAL_400_1"));
    }

    @Test
    @DisplayName("프로젝트가 REQUESTED 상태가 아니면 제안을 수락할 수 없다")
    void acceptProposalFailsWhenProjectStatusIsNotRequested() throws Exception {
        User owner = userRepository.save(createUser("owner-project-status@test.com", "프로젝트 사용자", "ROLE_USER"));
        User freelancerUser = userRepository.save(createUser("freelancer-project-status@test.com", "프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile freelancerProfile = freelancerProfileRepository.save(createProfile(freelancerUser, true));
        Project project = projectRepository.save(createProject(owner));
        Proposal proposal = proposalRepository.saveAndFlush(Proposal.create(project, freelancerProfile, "수락 시도"));
        project.accept(LocalDateTime.of(2026, 4, 15, 10, 0));
        projectRepository.saveAndFlush(project);

        mockMvc.perform(patch("/api/v1/freelancers/me/proposals/{proposalId}/accept", proposal.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_1"));
    }

    private User createUser(String email, String name, String roleCode) {
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("1234"))
                .name(name)
                .phone("010-0000-0000")
                .intro("소개")
                .roleCode(roleCode)
                .activeYn(true)
                .build();
    }

    private FreelancerProfile createProfile(User user, boolean publicYn) {
        return FreelancerProfile.create(
                user,
                "경력 설명",
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
                "병원 동행 요청",
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 14, 0),
                LocalDateTime.of(2026, 4, 20, 17, 0),
                "서울 강남구 테헤란로 123",
                "3층 접수대 앞",
                "병원 접수와 진료 동행이 필요합니다."
        );
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                List.of(new SimpleGrantedAuthority(user.getRoleCode()))
        );
    }
}
