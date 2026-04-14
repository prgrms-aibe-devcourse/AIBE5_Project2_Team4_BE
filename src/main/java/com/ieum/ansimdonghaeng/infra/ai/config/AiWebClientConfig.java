package com.ieum.ansimdonghaeng.infra.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class AiWebClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public WebClient aiWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(aiProperties.getBaseUrl())
                .build();
    }
}
