package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthRefreshRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthSignupRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthSignupResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthUserResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;
import com.ieum.ansimdonghaeng.domain.auth.entity.RefreshToken;
import com.ieum.ansimdonghaeng.domain.auth.oauth.KakaoOAuthClient;
import com.ieum.ansimdonghaeng.domain.auth.repository.RefreshTokenRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.AuthProvider;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoOAuthClient kakaoOAuthClient;

    @Transactional
    public AuthTokenResponse issueToken(AuthLoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        validateActiveUser(user);
        return issueTokensForUser(user);
    }

    @Transactional
    public AuthTokenResponse refresh(AuthRefreshRequest request) {
        String refreshTokenValue = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshTokenValue) || !"refresh".equals(jwtTokenProvider.getTokenType(refreshTokenValue))) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (!savedRefreshToken.isUsable(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtTokenProvider.getUsername(refreshTokenValue);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        validateActiveUser(user);
        savedRefreshToken.revoke(LocalDateTime.now());
        return issueTokensForUser(user);
    }

    @Transactional
    public AuthSignupResponse signup(AuthSignupRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .intro(request.intro())
                .roleCode(UserRole.USER.getCode())
                .activeYn(true)
                .build();

        User savedUser = userRepository.save(user);
        return new AuthSignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole().getCode());
    }

    @Transactional
    public AuthTokenResponse kakaoLogin(KakaoOAuthLoginRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(request.accessToken());
        String normalizedEmail = normalizeEmail(kakaoUserInfo.email());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> createKakaoUser(kakaoUserInfo));

        validateActiveUser(user);
        return issueTokensForUser(user);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));
        revokeActiveRefreshTokens(user, LocalDateTime.now());
    }

    private AuthTokenResponse issueTokensForUser(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getCode()));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), authorities);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getEmail(), authorities);
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.ofInstant(
                Instant.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationSeconds()),
                ZoneId.systemDefault()
        );
        refreshTokenRepository.save(RefreshToken.issue(user, refreshTokenValue, refreshTokenExpiresAt));

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                refreshTokenValue,
                jwtTokenProvider.getRefreshTokenExpirationSeconds(),
                new AuthUserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole().getCode())
        );
    }

    private void revokeActiveRefreshTokens(User user, LocalDateTime revokedAt) {
        refreshTokenRepository.findAllByUser_IdAndActiveYnTrue(user.getId())
                .forEach(refreshToken -> refreshToken.revoke(revokedAt));
    }

    private User createKakaoUser(KakaoUserInfo kakaoUserInfo) {
        String normalizedEmail = normalizeEmail(kakaoUserInfo.email());
        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(AuthProvider.KAKAO.getCode() + ":" + kakaoUserInfo.providerId()))
                .name(kakaoUserInfo.nickname())
                .roleCode(UserRole.USER.getCode())
                .activeYn(true)
                .build();

        return userRepository.save(user);
    }

    private void validateActiveUser(User user) {
        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
