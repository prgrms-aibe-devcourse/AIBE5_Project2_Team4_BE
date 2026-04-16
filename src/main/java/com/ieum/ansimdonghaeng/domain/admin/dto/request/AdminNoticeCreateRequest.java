package com.ieum.ansimdonghaeng.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminNoticeCreateRequest(
        @NotBlank(message = "title is required.")
        String title,
        @NotBlank(message = "content is required.")
        String content,
        Boolean publishNow
) {
}
