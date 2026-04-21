package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @Email(message = "invalid email format")
        @NotBlank(message = "email is required")
        @Size(max = 510, message = "email must be 510 characters or fewer")
        String email
) {
}