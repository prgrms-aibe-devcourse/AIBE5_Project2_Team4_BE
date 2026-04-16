package com.ieum.ansimdonghaeng.domain.admin.dto.response;

public record AdminReviewVisibilityResponse(
        Long reviewId,
        Boolean blindedYn
) {
}
