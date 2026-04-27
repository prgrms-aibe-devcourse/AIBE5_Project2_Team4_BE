package com.ieum.ansimdonghaeng.domain.recommendation.dto.response;

public record RecommendationWeightsResponse(
        Integer projectTypeWeight,
        Integer regionWeight,
        Integer timeSlotWeight,
        Integer verifiedWeight,
        Integer ratingWeight,
        Integer activityWeight
) {
}
