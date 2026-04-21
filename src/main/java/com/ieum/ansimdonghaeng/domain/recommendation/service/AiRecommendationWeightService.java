package com.ieum.ansimdonghaeng.domain.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.infra.ai.client.AiClient;
import com.ieum.ansimdonghaeng.infra.ai.config.AiProperties;
import com.ieum.ansimdonghaeng.infra.ai.dto.AiChatRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
class AiRecommendationWeightService {

    private final AiProperties aiProperties;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    Optional<RecommendationWeights> requestWeights(RecommendationServiceImpl.RecommendationTarget target) {
        if (!aiProperties.isEnabled()) {
            return Optional.empty();
        }

        try {
            String result = aiClient.requestChatText(new AiChatRequest(prompt(target))).block();
            if (!StringUtils.hasText(result)) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(extractJson(result));
            return Optional.of(RecommendationWeights.normalized(
                    root.path("projectTypeWeight").asInt(30),
                    root.path("regionWeight").asInt(25),
                    root.path("timeSlotWeight").asInt(15),
                    root.path("verifiedWeight").asInt(15),
                    root.path("ratingWeight").asInt(10),
                    root.path("activityWeight").asInt(5)
            ));
        } catch (RuntimeException | java.io.IOException ignored) {
            return Optional.empty();
        }
    }

    private String prompt(RecommendationServiceImpl.RecommendationTarget target) {
        return """
                You are tuning weights for a caregiver-freelancer matching score.
                Return JSON only. No markdown. No explanation.
                Each value must be an integer from 0 to 50.
                The backend will normalize the total to 100.
                Required keys:
                projectTypeWeight, regionWeight, timeSlotWeight, verifiedWeight, ratingWeight, activityWeight.

                Match request:
                projectTypeCode=%s
                serviceRegionCode=%s
                timeSlotCode=%s
                requestedStartAt=%s
                requestedEndAt=%s
                """.formatted(
                target.projectTypeCode(),
                target.serviceRegionCode(),
                target.timeSlotCode(),
                target.requestedStartAt(),
                target.requestedEndAt()
        );
    }

    private String extractJson(String value) {
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return value.substring(start, end + 1);
        }
        return value;
    }
}
