package com.ieum.ansimdonghaeng.domain.auth.dto.request;

public record KakaoOAuthLoginRequest(
        String accessToken,
        String authorizationCode
) {
}
