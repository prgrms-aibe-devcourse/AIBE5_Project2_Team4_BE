package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminNoticeUpdateRequest(
        @Pattern(regexp = "(?s)^.*\\S.*$", message = "title must not be blank")
        @Size(max = 400, message = "title must be 400 characters or fewer")
        String title,

        @Pattern(regexp = "(?s)^.*\\S.*$", message = "content must not be blank")
        String content
) {

    public boolean hasChanges() {
        return title != null || content != null;
    }
}
