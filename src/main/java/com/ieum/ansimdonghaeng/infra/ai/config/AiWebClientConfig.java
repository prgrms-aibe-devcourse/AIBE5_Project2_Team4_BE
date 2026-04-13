package com.ieum.ansimdonghaeng.infra.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiWebClientConfig {

    @Bean
    public WebClient aiWebClient(WebClient.Builder webClientBuilder,
                                 @Value("${app.ai.base-url:https://example.invalid}") String baseUrl) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
}
