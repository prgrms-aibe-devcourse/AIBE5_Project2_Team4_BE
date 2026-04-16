package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
        @JsonAlias("username")
        @Email(message = "invalid email format")
        @NotBlank(message = "email is required")
        @Size(max = 255, message = "email must be 255 characters or fewer")
        String email,
        @NotBlank(message = "password is required")
        String password
) {
}
