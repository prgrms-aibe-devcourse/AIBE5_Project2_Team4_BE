package com.ieum.ansimdonghaeng.domain.auth.oauth;

import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;

public interface KakaoOAuthClient {

    String getAccessToken(String authorizationCode);

    KakaoUserInfo getUserInfo(String accessToken);
}
