package com.ieum.ansimdonghaeng.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtPropertiesBindingTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void bindsJwtPropertiesFromConfiguration() {
        assertThat(jwtProperties.getHeader()).isEqualTo("Authorization");
        assertThat(jwtProperties.getPrefix()).isEqualTo("Bearer");
        assertThat(jwtProperties.getIssuer()).isEqualTo("ansimdonghaeng");
        assertThat(jwtProperties.getSecret()).hasSizeGreaterThanOrEqualTo(32);
        assertThat(jwtProperties.getAccessTokenExpirationMinutes()).isPositive();
        assertThat(jwtProperties.getRefreshTokenExpirationMinutes()).isPositive();
    }
}
