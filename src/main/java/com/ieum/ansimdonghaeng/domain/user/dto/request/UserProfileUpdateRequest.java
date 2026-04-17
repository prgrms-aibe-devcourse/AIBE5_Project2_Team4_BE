package com.ieum.ansimdonghaeng.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be 200 characters or fewer")
        String name,

        @Size(max = 40, message = "phone must be 40 characters or fewer")
        @Pattern(regexp = "^[0-9+\\-() ]*$", message = "phone has invalid characters")
        String phone,

        @Size(max = 500, message = "intro must be 500 characters or fewer")
        String intro
) {
}
