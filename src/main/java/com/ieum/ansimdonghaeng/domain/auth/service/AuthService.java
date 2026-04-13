package com.ieum.ansimdonghaeng.domain.auth.service;

import com.ieum.ansimdonghaeng.common.jwt.JwtProperties;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetailsService;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
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
}
