package com.ieum.ansimdonghaeng.infra.ai.client;

import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatRequest;
import com.ieum.ansimdonghaeng.infra.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;
    private final AiProperties aiProperties;

    public Mono<String> requestChat(AiChatRequest request) {
        return aiWebClient.post()
                .uri("/v1/responses")
                .bodyValue(new OpenAIResponsesRequest(aiProperties.getModel(), request.prompt()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::extractOutputText);
    }

    private String extractOutputText(JsonNode response) {
        if (response == null || response.isNull()) {
            return "";
        }

        JsonNode outputText = response.path("output_text");
        if (outputText.isTextual()) {
            String text = outputText.asText("");
            if (!text.isBlank()) {
                return text;
            }
        }

        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }
                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    String type = contentItem.path("type").asText();
                    if ("output_text".equals(type) || "text".equals(type)) {
                        String text = contentItem.path("text").asText("");
                        if (!text.isBlank()) {
                            return text;
                        }
                    }
                }
            }
        }

        return "";
    }

    private record OpenAIResponsesRequest(String model, String input) {
    }
}
