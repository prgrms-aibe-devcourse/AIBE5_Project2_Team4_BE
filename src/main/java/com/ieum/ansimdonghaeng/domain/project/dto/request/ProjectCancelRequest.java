package com.ieum.ansimdonghaeng.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 프로젝트 취소 사유를 전달한다.
public record ProjectCancelRequest(
        @NotBlank(message = "reason is required")
        @Size(max = 500, message = "reason must be 500 characters or fewer")
        String reason
) {
}
