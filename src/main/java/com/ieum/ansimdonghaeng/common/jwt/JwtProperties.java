package com.ieum.ansimdonghaeng.common.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank
    @Size(min = 32)
    private String secret;

    @NotBlank
    private String issuer;

    @Positive
    private long accessTokenExpirationMinutes;

    @Positive
    private long refreshTokenExpirationMinutes;

    @NotBlank
    private String header;

    @NotBlank
    private String prefix;
}
