package com.ieum.ansimdonghaeng.domain.freelancer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FreelancerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("공개 프리랜서 목록은 인증 없이 조회할 수 있다")
    void getFreelancersWithoutAuthentication() throws Exception {
        User publicFreelancer = userRepository.save(createUser("freelancer1@test.com", "공개 프리랜서", "ROLE_FREELANCER"));
        User privateFreelancer = userRepository.save(createUser("freelancer2@test.com", "비공개 프리랜서", "ROLE_FREELANCER"));

        FreelancerProfile publicProfile = freelancerProfileRepository.save(createProfile(publicFreelancer, true));
        freelancerProfileRepository.save(createProfile(privateFreelancer, false));

        mockMvc.perform(get("/api/v1/freelancers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].freelancerProfileId").value(publicProfile.getId()))
                .andExpect(jsonPath("$.data.content[0].name").value("공개 프리랜서"));
    }

    @Test
    @DisplayName("공개 프리랜서 상세는 인증 없이 조회할 수 있다")
    void getFreelancerDetailWithoutAuthentication() throws Exception {
        User freelancer = userRepository.save(createUser("detail@test.com", "상세 프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile profile = freelancerProfileRepository.save(createProfile(freelancer, true));

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}", profile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.freelancerProfileId").value(profile.getId()))
                .andExpect(jsonPath("$.data.userId").value(freelancer.getId()))
                .andExpect(jsonPath("$.data.name").value("상세 프리랜서"))
                .andExpect(jsonPath("$.data.publicYn").value(true));
    }

    @Test
    @DisplayName("비공개 프리랜서 상세는 조회할 수 없다")
    void getFreelancerDetailFailsForPrivateProfile() throws Exception {
        User freelancer = userRepository.save(createUser("private@test.com", "비공개 프리랜서", "ROLE_FREELANCER"));
        FreelancerProfile profile = freelancerProfileRepository.save(createProfile(freelancer, false));

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}", profile.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FREELANCER_404_1"));
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
                12L,
                publicYn,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        );
    }
}
