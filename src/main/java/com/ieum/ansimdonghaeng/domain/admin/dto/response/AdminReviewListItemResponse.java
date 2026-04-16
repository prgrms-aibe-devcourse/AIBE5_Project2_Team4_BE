package com.ieum.ansimdonghaeng.domain.admin.dto.response;

import java.time.LocalDateTime;

public record AdminReviewListItemResponse(
        Long reviewId,
        Long projectId,
        String projectTitle,
        AdminUserSummaryResponse writer,
        AdminFreelancerSummaryResponse targetFreelancer,
        Integer rating,
        Boolean blindedYn,
        LocalDateTime createdAt
) {
}
