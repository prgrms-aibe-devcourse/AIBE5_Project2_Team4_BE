package com.ieum.ansimdonghaeng.infra.ai.client;

import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatRequest;
import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;

    public Mono<AiChatResponse> requestChat(AiChatRequest request) {
        return aiWebClient.post()
                .uri("/v1/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiChatResponse.class);
    }
}
