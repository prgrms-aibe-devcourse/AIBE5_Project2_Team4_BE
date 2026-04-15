package com.ieum.ansimdonghaeng.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;
import com.ieum.ansimdonghaeng.domain.auth.oauth.KakaoOAuthClient;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.AuthProvider;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class KakaoOAuthServiceTest {

    @Autowired
    private AuthService authService;

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

    @MockBean
    private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("kakao login creates user and returns service tokens")
    void kakaoLoginCreatesUserAndReturnsTokens() {
        when(kakaoOAuthClient.getUserInfo("kakao-access-token"))
                .thenReturn(new KakaoUserInfo("12345", "kakao-user@test.com", "kakao-user"));

        AuthTokenResponse response = authService.kakaoLogin(new KakaoOAuthLoginRequest("kakao-access-token"));

        Optional<User> savedUser = userRepository.findByProviderCodeAndProviderUserId(
                AuthProvider.KAKAO.getCode(),
                "12345"
        );

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("kakao-user@test.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("kakao-user@test.com");
        assertThat(savedUser.get().getName()).isEqualTo("kakao-user");
        assertThat(savedUser.get().getProviderCode()).isEqualTo(AuthProvider.KAKAO.getCode());
        assertThat(savedUser.get().getProviderUserId()).isEqualTo("12345");
        assertThat(savedUser.get().getRoleCode()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("kakao login rejects email already registered with local provider")
    void kakaoLoginRejectsLocalEmailConflict() {
        userRepository.save(User.builder()
                .email("conflict@test.com")
                .passwordHash(passwordEncoder.encode("1234"))
                .name("local-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        when(kakaoOAuthClient.getUserInfo("conflict-token"))
                .thenReturn(new KakaoUserInfo("99999", "conflict@test.com", "kakao-user"));

        assertThatThrownBy(() -> authService.kakaoLogin(new KakaoOAuthLoginRequest("conflict-token")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OAUTH_ACCOUNT_CONFLICT);
    }
}
