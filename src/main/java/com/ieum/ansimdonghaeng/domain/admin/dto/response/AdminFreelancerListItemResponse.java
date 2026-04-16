package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminFreelancerListItemResponse(
        Long freelancerProfileId,
        Long userId,
        String name,
        String email,
        Boolean verifiedYn,
        Boolean publicYn,
        Boolean activeYn,
        BigDecimal averageRating,
        Long activityCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
