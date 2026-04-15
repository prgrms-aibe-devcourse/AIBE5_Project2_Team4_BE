package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

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

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 성공 시 access token을 발급한다")
    void issueToken_success() {
        // given
        User user = User.builder()
            .email("user@test.com")
            .passwordHash(passwordEncoder.encode("1234"))
            .name("테스트유저")
            .phone("010-1234-5678")
            .intro("소개")
            .roleCode("ROLE_USER")
            .activeYn(true)
            .build();

        userRepository.save(user);

        AuthLoginRequest request = new AuthLoginRequest("user@test.com", "1234");

        // when
        AuthTokenResponse response = authService.issueToken(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isGreaterThan(0);
        assertThat(response.refreshTokenExpiresInSeconds()).isGreaterThan(response.expiresInSeconds());
        assertThat(response.user().email()).isEqualTo("user@test.com");
    }
}
