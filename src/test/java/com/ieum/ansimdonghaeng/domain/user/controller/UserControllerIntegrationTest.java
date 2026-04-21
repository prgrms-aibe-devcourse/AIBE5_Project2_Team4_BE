package com.ieum.ansimdonghaeng.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.dto.request.UserProfileUpdateRequest;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.util.List;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 제안 -> 프리랜서 프로필 -> 사용자 순서로 FK를 따라 정리한다.
        proposalRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("public profile is available without login")
    void getPublicProfileWithoutAuthentication() throws Exception {
        User user = userRepository.save(createUser("public@test.com", "public-user", "ROLE_USER"));

        mockMvc.perform(get("/api/v1/users/{userId}/public-profile", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("public-user"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_USER"));
    }

    @Test
    @DisplayName("my profile requires login")
    void getMyProfileRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("my profile update changes the authenticated user")
    void updateMyProfileUpdatesCurrentUser() throws Exception {
        User user = userRepository.save(createUser("me@test.com", "before-name", "ROLE_USER"));
        String token = bearerToken(user.getEmail(), user.getRoleCode());

        UserProfileUpdateRequest request = new UserProfileUpdateRequest("after-name", "010-3333-4444", "after-intro");

        mockMvc.perform(patch("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("after-name"))
                .andExpect(jsonPath("$.data.phone").value("010-3333-4444"))
                .andExpect(jsonPath("$.data.intro").value("after-intro"));
    }

    @Test
    @DisplayName("debug admin access-check endpoint is removed")
    void adminAccessCheckEndpointRemoved() throws Exception {
        User admin = userRepository.save(createUser("admin@test.com", "admin-user", "ROLE_ADMIN"));
        String token = bearerToken(admin.getEmail(), admin.getRoleCode());

        mockMvc.perform(get("/api/v1/admin/access-check")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("debug freelancer workspace endpoint is removed")
    void freelancerWorkspaceEndpointRemoved() throws Exception {
        User user = userRepository.save(createUser("freelancer@test.com", "freelancer-user", "ROLE_FREELANCER"));
        String token = bearerToken(user.getEmail(), user.getRoleCode());

        mockMvc.perform(get("/api/v1/freelancers/me/workspace")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
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

    private String bearerToken(String email, String roleCode) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(email, List.of(new SimpleGrantedAuthority(roleCode)));
    }
}
