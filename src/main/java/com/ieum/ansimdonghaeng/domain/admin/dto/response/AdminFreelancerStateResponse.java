package com.ieum.ansimdonghaeng.domain.admin.dto.response;

public record AdminFreelancerStateResponse(
        Long freelancerProfileId,
        Boolean publicYn,
        Boolean activeYn,
        Boolean verifiedYn
) {
}
