package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthSignupRequest(
        @NotBlank(message = "username is required")
        String username,
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "role is required")
        UserRole role
) {
}
