package com.ieum.ansimdonghaeng.infra.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.ieum.ansimdonghaeng.infra.ai.config.AiProperties;
import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatRequest;
import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;
    private final AiProperties aiProperties;

    public Mono<AiChatResponse> requestChat(AiChatRequest request) {
        return aiWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(openAiChatCompletionsRequest(request.prompt()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> new AiChatResponse(response
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText()));
    }

    public Mono<String> requestChatText(AiChatRequest request) {
        return requestChat(request).map(AiChatResponse::result);
    }

    private Map<String, Object> openAiChatCompletionsRequest(String prompt) {
        return Map.of(
                "model", aiProperties.getModel(),
                "temperature", 0.2,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Return JSON only. Do not use markdown."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );
    }
}
