package com.ieum.ansimdonghaeng.domain.recommendation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record FreelancerRecommendationRequest(
        Long projectId,
        String projectTypeCode,
        String serviceRegionCode,
        String timeSlotCode,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        @Min(1) @Max(20) Integer size
) {
}
