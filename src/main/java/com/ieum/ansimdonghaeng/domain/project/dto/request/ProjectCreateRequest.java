package com.ieum.ansimdonghaeng.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// 프로젝트 생성 요청 본문을 정의한다.
public record ProjectCreateRequest(
        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be 200 characters or fewer")
        String title,
        @NotBlank(message = "projectTypeCode is required")
        @Size(max = 30, message = "projectTypeCode must be 30 characters or fewer")
        String projectTypeCode,
        @NotBlank(message = "serviceRegionCode is required")
        @Size(max = 20, message = "serviceRegionCode must be 20 characters or fewer")
        String serviceRegionCode,
        @NotNull(message = "requestedStartAt is required")
        LocalDateTime requestedStartAt,
        @NotNull(message = "requestedEndAt is required")
        LocalDateTime requestedEndAt,
        @NotBlank(message = "serviceAddress is required")
        @Size(max = 300, message = "serviceAddress must be 300 characters or fewer")
        String serviceAddress,
        @Size(max = 300, message = "serviceDetailAddress must be 300 characters or fewer")
        String serviceDetailAddress,
        @NotBlank(message = "requestDetail is required")
        @Size(max = 2000, message = "requestDetail must be 2000 characters or fewer")
        String requestDetail
) {
}
