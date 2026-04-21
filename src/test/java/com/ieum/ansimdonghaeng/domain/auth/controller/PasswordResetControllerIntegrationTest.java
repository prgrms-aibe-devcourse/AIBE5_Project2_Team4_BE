package com.ieum.ansimdonghaeng.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthRefreshRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ForgotPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ResetPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.auth.store.PasswordResetTokenStore;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class PasswordResetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenStore tokenStore;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("존재하는 이메일로 forgot-password 요청 시 200 반환")
    void forgotPassword_existingEmail_returns200() throws Exception {
        userRepository.save(User.builder()
                .email("forgot@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("forgot-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("forgot@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 forgot-password 요청해도 200 반환 (이메일 존재 여부 노출 금지)")
    void forgotPassword_nonExistingEmail_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("nobody@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 forgot-password 요청 시 400 반환")
    void forgotPassword_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("유효한 토큰으로 reset-password 요청 시 200 반환 및 비밀번호 변경")
    void resetPassword_validToken_returns200AndChangesPassword() throws Exception {
        userRepository.save(User.builder()
                .email("reset@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("reset-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = tokenStore.createToken("reset@test.com", 10);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "newPass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 새 비밀번호로 로그인 성공 확인
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest("reset@test.com", "newPass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 후 기존 refresh token은 사용할 수 없다")
    void resetPassword_validToken_revokesExistingRefreshToken() throws Exception {
        userRepository.save(User.builder()
                .email("reset-refresh@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("reset-refresh-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest("reset-refresh@test.com", "oldPass1!"))))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("refreshToken")
                .asText();
        String resetToken = tokenStore.createToken("reset-refresh@test.com", 10);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(resetToken, "newPass1!"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_401_REFRESH"));
    }

    @Test
    @DisplayName("72자를 초과하는 새 비밀번호로 reset-password 요청 시 400 반환")
    void resetPassword_tooLongPassword_returns400() throws Exception {
        String token = tokenStore.createToken("too-long@test.com", 10);
        String tooLongPassword = "a".repeat(73);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, tooLongPassword))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("잘못된 resetToken으로 reset-password 요청 시 400 반환")
    void resetPassword_invalidToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("invalid-token", "newPass1!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_400_RESET"));
    }

    @Test
    @DisplayName("만료된 resetToken으로 reset-password 요청 시 400 반환")
    void resetPassword_expiredToken_returns400() throws Exception {
        userRepository.save(User.builder()
                .email("expired@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("expired-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = tokenStore.createToken("expired@test.com", -1);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "newPass1!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_400_RESET"));
    }

    @Test
    @DisplayName("이미 사용된 resetToken으로 재요청 시 400 반환")
    void resetPassword_alreadyUsedToken_returns400() throws Exception {
        userRepository.save(User.builder()
                .email("used@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("used-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = tokenStore.createToken("used@test.com", 10);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "newPass1!"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "anotherPass1!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_400_RESET"));
    }
}
