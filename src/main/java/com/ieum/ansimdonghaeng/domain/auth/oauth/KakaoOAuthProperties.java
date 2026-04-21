package com.ieum.ansimdonghaeng.domain.auth.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.oauth.kakao")
public class KakaoOAuthProperties {

    @NotBlank
    private String tokenUri;

    @NotBlank
    private String restApiKey;

    private String clientSecret;

    @NotBlank
    private String redirectUri;

    @NotBlank
    private String userInfoUri;

    @Positive
    private int connectTimeoutMillis = 3000;

    @Positive
    private int responseTimeoutMillis = 5000;
}
