package com.ieum.ansimdonghaeng.infra.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    @NotBlank
    private String baseUrl;

    private boolean enabled = false;

    private String apiKey;

    private String model = "gpt-4o";

    @Positive
    private int connectTimeoutMillis = 3000;

    @Positive
    private int responseTimeoutMillis = 5000;
}
