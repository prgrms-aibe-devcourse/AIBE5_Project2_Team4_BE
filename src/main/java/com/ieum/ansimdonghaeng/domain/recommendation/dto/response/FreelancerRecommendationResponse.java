package com.ieum.ansimdonghaeng.domain.recommendation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record FreelancerRecommendationResponse(
        Long projectId,
        String projectTypeCode,
        String serviceRegionCode,
        String timeSlotCode,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        Boolean aiApplied,
        String scoringMode,
        RecommendationWeightsResponse weights,
        Integer totalCandidates,
        List<FreelancerRecommendationItemResponse> recommendations
) {
}
