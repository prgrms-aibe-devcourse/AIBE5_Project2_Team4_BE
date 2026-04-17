package com.ieum.ansimdonghaeng.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// 프로젝트 생성 요청 본문을 정의한다.
public record ProjectCreateRequest(
        @NotBlank(message = "title is required")
        @Size(max = 400, message = "title must be 400 characters or fewer")
        String title,
        @NotBlank(message = "projectTypeCode is required")
        @Size(max = 60, message = "projectTypeCode must be 60 characters or fewer")
        String projectTypeCode,
        @NotBlank(message = "serviceRegionCode is required")
        @Size(max = 40, message = "serviceRegionCode must be 40 characters or fewer")
        String serviceRegionCode,
        @NotNull(message = "requestedStartAt is required")
        LocalDateTime requestedStartAt,
        @NotNull(message = "requestedEndAt is required")
        LocalDateTime requestedEndAt,
        @NotBlank(message = "serviceAddress is required")
        @Size(max = 600, message = "serviceAddress must be 600 characters or fewer")
        String serviceAddress,
        @Size(max = 600, message = "serviceDetailAddress must be 600 characters or fewer")
        String serviceDetailAddress,
        @NotBlank(message = "requestDetail is required")
        @Size(max = 4000, message = "requestDetail must be 4000 characters or fewer")
        String requestDetail
) {
}
