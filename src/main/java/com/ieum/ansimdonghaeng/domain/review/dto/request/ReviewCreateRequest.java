package com.ieum.ansimdonghaeng.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewCreateRequest(

    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating must be at most 5")
    Integer rating,

    String tag,

    @NotBlank(message = "content is required")
    String content
) {
}
