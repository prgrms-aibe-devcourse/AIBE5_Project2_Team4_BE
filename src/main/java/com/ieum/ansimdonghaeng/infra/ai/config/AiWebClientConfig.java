package com.ieum.ansimdonghaeng.infra.ai.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class AiWebClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public WebClient aiWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, aiProperties.getConnectTimeoutMillis())
                        .responseTimeout(Duration.ofMillis(aiProperties.getResponseTimeoutMillis()))))
                .baseUrl(aiProperties.getBaseUrl())
                .defaultHeaders(headers -> {
                    if (StringUtils.hasText(aiProperties.getApiKey())) {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey());
                    }
                })
                .build();
    }
}
