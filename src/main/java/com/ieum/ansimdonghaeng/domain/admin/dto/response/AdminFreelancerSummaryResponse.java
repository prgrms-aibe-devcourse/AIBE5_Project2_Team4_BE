package com.ieum.ansimdonghaeng.domain.admin.dto.response;

public record AdminFreelancerSummaryResponse(
        Long freelancerProfileId,
        Long userId,
        String name,
        String email,
        Boolean verifiedYn,
        Boolean publicYn,
        Boolean activeYn
) {
}
