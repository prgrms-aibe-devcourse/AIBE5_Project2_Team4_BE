package com.ieum.ansimdonghaeng.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthRefreshRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthSignupRequest;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("signup returns created user info")
    void signupReturnsCreatedUser() throws Exception {
        AuthSignupRequest request = new AuthSignupRequest("new-user@test.com", "1234", "new-user", "010-1111-2222", "intro");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new-user@test.com"))
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_USER"));
    }

    @Test
    @DisplayName("login returns access and refresh tokens")
    void loginReturnsAccessToken() throws Exception {
        userRepository.save(User.builder()
                .email("login@test.com")
                .passwordHash(passwordEncoder.encode("1234"))
                .name("login-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        AuthLoginRequest request = new AuthLoginRequest("login@test.com", "1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("login@test.com"));
    }

    @Test
    @DisplayName("refresh rotates refresh token and returns a new token pair")
    void refreshReturnsNewTokenPair() throws Exception {
        userRepository.save(User.builder()
                .email("refresh@test.com")
                .passwordHash(passwordEncoder.encode("1234"))
                .name("refresh-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest("refresh@test.com", "1234"))))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("refreshToken")
                .asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("refresh@test.com"));
    }

    @Test
    @DisplayName("logout requires valid authentication")
    void logoutRequiresAuthentication() throws Exception {
        userRepository.save(User.builder()
                .email("logout@test.com")
                .passwordHash(passwordEncoder.encode("1234"))
                .name("logout-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = "Bearer " + jwtTokenProvider.generateAccessToken(
                "logout@test.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
