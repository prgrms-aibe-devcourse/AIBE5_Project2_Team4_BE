package com.ieum.ansimdonghaeng.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ieum.ansimdonghaeng.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;
import com.ieum.ansimdonghaeng.domain.auth.oauth.KakaoOAuthClient;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
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
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("kakao login creates user and returns service tokens")
    void kakaoLoginCreatesUserAndReturnsTokens() {
        when(kakaoOAuthClient.getUserInfo("kakao-access-token"))
                .thenReturn(new KakaoUserInfo("12345", "kakao-user@test.com", "kakao-user"));

        AuthTokenResponse response = authService.kakaoLogin(new KakaoOAuthLoginRequest("kakao-access-token", null));

        Optional<User> savedUser = userRepository.findByEmailIgnoreCase("kakao-user@test.com");

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("kakao-user@test.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("kakao-user@test.com");
        assertThat(savedUser.get().getName()).isEqualTo("kakao-user");
        assertThat(savedUser.get().getRoleCode()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("kakao login exchanges authorization code before user info lookup")
    void kakaoLoginExchangesAuthorizationCode() {
        when(kakaoOAuthClient.getAccessToken("kakao-authorization-code"))
                .thenReturn("kakao-access-token");
        when(kakaoOAuthClient.getUserInfo("kakao-access-token"))
                .thenReturn(new KakaoUserInfo("12345", "kakao-code-user@test.com", "kakao-code-user"));

        AuthTokenResponse response = authService.kakaoLogin(new KakaoOAuthLoginRequest(null, "kakao-authorization-code"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("kakao-code-user@test.com");
    }

    @Test
    @DisplayName("kakao login reuses an existing email-matched account")
    void kakaoLoginReusesExistingEmailMatchedAccount() {
        User existingUser = userRepository.save(User.builder()
                .email("existing@test.com")
                .passwordHash(passwordEncoder.encode("1234"))
                .name("local-user")
                .roleCode("ROLE_USER")
                .activeYn(true)
                .build());

        when(kakaoOAuthClient.getUserInfo("existing-token"))
                .thenReturn(new KakaoUserInfo("99999", "existing@test.com", "kakao-user"));

        AuthTokenResponse response = authService.kakaoLogin(new KakaoOAuthLoginRequest("existing-token", null));

        assertThat(response.user().userId()).isEqualTo(existingUser.getId());
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
