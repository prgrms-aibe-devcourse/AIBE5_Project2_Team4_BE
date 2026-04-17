package com.ieum.ansimdonghaeng.domain.project.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// 프로젝트 부분 수정 요청 본문을 정의한다.
public record ProjectUpdateRequest(
        @Pattern(regexp = "^(?=.*\\S).+$", message = "title must not be blank")
        @Size(max = 400, message = "title must be 400 characters or fewer")
        String title,
        @Pattern(regexp = "^(?=.*\\S).+$", message = "projectTypeCode must not be blank")
        @Size(max = 60, message = "projectTypeCode must be 60 characters or fewer")
        String projectTypeCode,
        @Pattern(regexp = "^(?=.*\\S).+$", message = "serviceRegionCode must not be blank")
        @Size(max = 40, message = "serviceRegionCode must be 40 characters or fewer")
        String serviceRegionCode,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        @Pattern(regexp = "^(?=.*\\S).+$", message = "serviceAddress must not be blank")
        @Size(max = 600, message = "serviceAddress must be 600 characters or fewer")
        String serviceAddress,
        @Pattern(regexp = "^(?=.*\\S).+$", message = "serviceDetailAddress must not be blank")
        @Size(max = 600, message = "serviceDetailAddress must be 600 characters or fewer")
        String serviceDetailAddress,
        @Pattern(regexp = "^(?=.*\\S).+$", message = "requestDetail must not be blank")
        @Size(max = 4000, message = "requestDetail must be 4000 characters or fewer")
        String requestDetail
) {

    // 부분 수정은 최소 한 필드 이상 들어와야 한다.
    public boolean hasChanges() {
        return title != null
                || projectTypeCode != null
                || serviceRegionCode != null
                || requestedStartAt != null
                || requestedEndAt != null
                || serviceAddress != null
                || serviceDetailAddress != null
                || requestDetail != null;
    }
}
