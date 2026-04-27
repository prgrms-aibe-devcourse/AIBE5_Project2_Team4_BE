package com.ieum.ansimdonghaeng.domain.recommendation;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void recommendFreelancersByProjectRanksBestCandidateFirst() throws Exception {
        User owner = userRepository.save(createUser("owner@test.com", "Owner", UserRole.USER, true));
        User bestUser = userRepository.save(createUser("best@test.com", "Best Freelancer", UserRole.FREELANCER, true));
        User weakerUser = userRepository.save(createUser("weaker@test.com", "Weaker Freelancer", UserRole.FREELANCER, true));

        FreelancerProfile bestProfile = freelancerProfileRepository.save(createProfile(
                bestUser,
                true,
                true,
                new BigDecimal("4.90"),
                35L,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        ));
        freelancerProfileRepository.save(createProfile(
                weakerUser,
                true,
                false,
                new BigDecimal("4.00"),
                3L,
                Set.of("SEOUL_GANGNAM"),
                Set.of("AFTERNOON"),
                Set.of("OTHER")
        ));
        Project project = projectRepository.save(Project.create(
                owner.getId(),
                "Hospital companion",
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 9, 0),
                LocalDateTime.of(2026, 4, 20, 12, 0),
                "Seoul Gangnam",
                null,
                "Need hospital companion"
        ));

        mockMvc.perform(post("/api/v1/recommendations/freelancers")
                        .with(authenticatedUser(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectId", project.getId(),
                                "timeSlotCode", "MORNING",
                                "size", 2
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(project.getId()))
                .andExpect(jsonPath("$.data.totalCandidates").value(2))
                .andExpect(jsonPath("$.data.recommendations[0].rank").value(1))
                .andExpect(jsonPath("$.data.recommendations[0].freelancerProfileId").value(bestProfile.getId()))
                .andExpect(jsonPath("$.data.recommendations[0].matchScore").value(100))
                .andExpect(jsonPath("$.data.recommendations[0].matchReasons[0]").value("PROJECT_TYPE_MATCH"));
    }

    @Test
    void recommendFreelancersRejectsOtherUsersProject() throws Exception {
        User owner = userRepository.save(createUser("owner2@test.com", "Owner", UserRole.USER, true));
        User other = userRepository.save(createUser("other@test.com", "Other", UserRole.USER, true));
        Project project = projectRepository.save(Project.create(
                owner.getId(),
                "Hospital companion",
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 9, 0),
                LocalDateTime.of(2026, 4, 20, 12, 0),
                "Seoul Gangnam",
                null,
                "Need hospital companion"
        ));

        mockMvc.perform(post("/api/v1/recommendations/freelancers")
                        .with(authenticatedUser(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("projectId", project.getId()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_403_1"));
    }

    @Test
    void recommendFreelancersPublicByConditionWithoutAuthentication() throws Exception {
        User freelancer = userRepository.save(createUser("public-ai@test.com", "Public AI Freelancer", UserRole.FREELANCER, true));
        FreelancerProfile profile = freelancerProfileRepository.save(createProfile(
                freelancer,
                true,
                true,
                new BigDecimal("4.80"),
                20L,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        ));

        mockMvc.perform(post("/api/v1/recommendations/freelancers/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectTypeCode", "HOSPITAL_COMPANION",
                                "serviceRegionCode", "SEOUL_GANGNAM",
                                "timeSlotCode", "MORNING",
                                "size", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiApplied").value(false))
                .andExpect(jsonPath("$.data.scoringMode").value("DEFAULT_WEIGHTED"))
                .andExpect(jsonPath("$.data.weights.projectTypeWeight").value(30))
                .andExpect(jsonPath("$.data.recommendations[0].freelancerProfileId").value(profile.getId()));
    }

    private User createUser(String email, String name, UserRole role, boolean active) {
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("1234"))
                .name(name)
                .phone("010-0000-0000")
                .intro("intro")
                .roleCode(role.getCode())
                .activeYn(active)
                .build();
    }

    private FreelancerProfile createProfile(
            User user,
            boolean publicYn,
            boolean verified,
            BigDecimal averageRating,
            Long activityCount,
            Set<String> activityRegionCodes,
            Set<String> availableTimeSlotCodes,
            Set<String> projectTypeCodes
    ) {
        return FreelancerProfile.create(
                user,
                "career",
                true,
                verified,
                averageRating,
                activityCount,
                publicYn,
                activityRegionCodes,
                availableTimeSlotCodes,
                projectTypeCodes
        );
    }

    private RequestPostProcessor authenticatedUser(User user) {
        return user(CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .password("{noop}password")
                .authorities(List.of(new SimpleGrantedAuthority(UserRole.USER.asAuthority())))
                .enabled(true)
                .build());
    }
}
