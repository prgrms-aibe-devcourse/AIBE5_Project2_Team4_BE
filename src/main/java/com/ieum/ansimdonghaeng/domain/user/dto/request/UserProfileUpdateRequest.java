package com.ieum.ansimdonghaeng.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be 100 characters or fewer")
        String name,

        @Size(max = 20, message = "phone must be 20 characters or fewer")
        @Pattern(regexp = "^[0-9+\\-() ]*$", message = "phone has invalid characters")
        String phone,

        @Size(max = 500, message = "intro must be 500 characters or fewer")
        String intro
) {
}
