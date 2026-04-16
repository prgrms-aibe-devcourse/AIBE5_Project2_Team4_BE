package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminFreelancerActiveRequest(
        @NotNull(message = "activeYn is required.")
        Boolean activeYn
) {
}
