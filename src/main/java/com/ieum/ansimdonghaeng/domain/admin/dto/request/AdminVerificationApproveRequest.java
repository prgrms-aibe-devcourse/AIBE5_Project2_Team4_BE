package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

public record AdminVerificationApproveRequest(
        @JsonAlias("comment")
        @Size(max = 4000, message = "reviewComment must be 4000 characters or fewer")
        String reviewComment
) {
}
