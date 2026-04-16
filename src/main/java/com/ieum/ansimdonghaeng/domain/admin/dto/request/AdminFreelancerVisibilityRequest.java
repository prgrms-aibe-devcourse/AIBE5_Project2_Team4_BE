package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminFreelancerVisibilityRequest(
        @NotNull(message = "publicYn is required.")
        Boolean publicYn
) {
}
