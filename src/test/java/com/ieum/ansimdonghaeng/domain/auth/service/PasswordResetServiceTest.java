package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ForgotPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ResetPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.entity.RefreshToken;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.auth.store.PasswordResetTokenStore;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PasswordResetServiceTest {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenStore tokenStore;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
    @DisplayName("존재하는 이메일로 sendResetEmail 호출 시 예외 없이 완료된다")
    void sendResetEmail_existingUser_completesWithoutException() {
        userRepository.save(User.builder()
                .email("reset@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("reset-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        passwordResetService.sendResetEmail(new ForgotPasswordRequest("reset@test.com"));
        // 예외 없이 완료되면 통과
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 sendResetEmail 호출 시 예외 없이 완료된다 (정보 노출 방지)")
    void sendResetEmail_nonExistingUser_completesWithoutException() {
        passwordResetService.sendResetEmail(new ForgotPasswordRequest("nobody@test.com"));
        // 예외 없이 완료되면 통과
    }

    @Test
    @DisplayName("유효한 resetToken으로 비밀번호를 변경할 수 있다")
    void resetPassword_validToken_updatesPassword() {
        User user = userRepository.save(User.builder()
                .email("pw-change@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("pw-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = tokenStore.createToken(user.getEmail(), 10);

        passwordResetService.resetPassword(new ResetPasswordRequest(token, "newPass1!"));

        User updated = userRepository.findByEmailIgnoreCase(user.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches("newPass1!", updated.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("oldPass1!", updated.getPasswordHash())).isFalse();
    }

    @Test
    @DisplayName("비밀번호 재설정 시 활성 refreshToken을 revoke한다")
    void resetPassword_validToken_revokesActiveRefreshTokens() {
        User user = userRepository.save(User.builder()
                .email("revoke-refresh@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("revoke-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.issue(
                user,
                "stored-refresh-token",
                LocalDateTime.now().plusDays(1)
        ));
        String resetToken = tokenStore.createToken(user.getEmail(), 10);

        passwordResetService.resetPassword(new ResetPasswordRequest(resetToken, "newPass1!"));

        RefreshToken revoked = refreshTokenRepository.findById(refreshToken.getId()).orElseThrow();
        assertThat(revoked.getActiveYn()).isFalse();
        assertThat(revoked.getRevokedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 resetToken이면 INVALID_RESET_TOKEN 예외 발생")
    void resetPassword_invalidToken_throwsException() {
        assertThatThrownBy(() ->
                passwordResetService.resetPassword(new ResetPasswordRequest("non-existent-token", "newPass1!"))
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RESET_TOKEN);
    }

    @Test
    @DisplayName("만료된 resetToken이면 INVALID_RESET_TOKEN 예외 발생")
    void resetPassword_expiredToken_throwsException() {
        User user = userRepository.save(User.builder()
                .email("expired@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("expired-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        // 이미 만료된 토큰 (만료 시간 -1분)
        String token = tokenStore.createToken(user.getEmail(), -1);

        assertThatThrownBy(() ->
                passwordResetService.resetPassword(new ResetPasswordRequest(token, "newPass1!"))
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RESET_TOKEN);
    }

    @Test
    @DisplayName("사용된 resetToken은 재사용 시 INVALID_RESET_TOKEN 예외 발생")
    void resetPassword_alreadyUsedToken_throwsException() {
        User user = userRepository.save(User.builder()
                .email("used-token@test.com")
                .passwordHash(passwordEncoder.encode("oldPass1!"))
                .name("used-token-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        String token = tokenStore.createToken(user.getEmail(), 10);

        passwordResetService.resetPassword(new ResetPasswordRequest(token, "newPass1!"));

        assertThatThrownBy(() ->
                passwordResetService.resetPassword(new ResetPasswordRequest(token, "anotherPass1!"))
        )
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_RESET_TOKEN);
    }
}
