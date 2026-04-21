package com.ieum.ansimdonghaeng.domain.recommendation.service;

import com.ieum.ansimdonghaeng.domain.recommendation.dto.response.RecommendationWeightsResponse;

record RecommendationWeights(
        int projectTypeWeight,
        int regionWeight,
        int timeSlotWeight,
        int verifiedWeight,
        int ratingWeight,
        int activityWeight
) {

    static RecommendationWeights defaults() {
        return new RecommendationWeights(30, 25, 15, 15, 10, 5);
    }

    static RecommendationWeights normalized(
            int projectTypeWeight,
            int regionWeight,
            int timeSlotWeight,
            int verifiedWeight,
            int ratingWeight,
            int activityWeight
    ) {
        int projectType = clamp(projectTypeWeight);
        int region = clamp(regionWeight);
        int timeSlot = clamp(timeSlotWeight);
        int verified = clamp(verifiedWeight);
        int rating = clamp(ratingWeight);
        int activity = clamp(activityWeight);

        int total = projectType + region + timeSlot + verified + rating + activity;
        if (total <= 0) {
            return defaults();
        }

        return new RecommendationWeights(
                scale(projectType, total),
                scale(region, total),
                scale(timeSlot, total),
                scale(verified, total),
                scale(rating, total),
                scale(activity, total)
        );
    }

    RecommendationWeightsResponse toResponse() {
        return new RecommendationWeightsResponse(
                projectTypeWeight,
                regionWeight,
                timeSlotWeight,
                verifiedWeight,
                ratingWeight,
                activityWeight
        );
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(50, value));
    }

    private static int scale(int value, int total) {
        return Math.round(value * 100.0f / total);
    }
}
