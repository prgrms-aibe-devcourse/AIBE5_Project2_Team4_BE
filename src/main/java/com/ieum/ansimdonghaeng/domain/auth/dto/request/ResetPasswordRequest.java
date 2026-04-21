package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "resetToken is required")
        String resetToken,
        @NotBlank(message = "newPassword is required")
        @Size(min = 8, max = 72, message = "newPassword must be between 8 and 72 characters")
        String newPassword
) {
}
