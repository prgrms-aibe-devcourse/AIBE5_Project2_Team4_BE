package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.jwt.JwtProperties;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetailsService;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLogoutRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthReissueRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthSignupRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthActionResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthSignupResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthSignupResponse signup(AuthSignupRequest request) {
        // TODO: Replace with persistence-backed signup when the auth domain is implemented.
        return new AuthSignupResponse(
                request.username(),
                request.role(),
                "Signup endpoint skeleton is ready."
        );
    }

    public AuthTokenResponse issueToken(AuthLoginRequest request) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.username());
        String accessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getUsername(),
                userDetails.getAuthorities()
        );

        return new AuthTokenResponse(
                jwtProperties.getPrefix(),
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    public AuthTokenResponse reissue(AuthReissueRequest request) {
        // TODO: Replace placeholder token reissue with refresh-token validation and persistence.
        return new AuthTokenResponse(
                jwtProperties.getPrefix(),
                "reissue-token-placeholder",
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    public AuthActionResponse logout(AuthLogoutRequest request) {
        // TODO: Replace placeholder logout with token invalidation when auth persistence is introduced.
        return new AuthActionResponse("Logout endpoint skeleton is ready.");
    }
}
