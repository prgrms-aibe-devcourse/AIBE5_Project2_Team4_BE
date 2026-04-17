package com.ieum.ansimdonghaeng.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthSignupRequest(

    @Email(message = "invalid email format")
    @NotBlank(message = "email is required")
    @Size(max = 510, message = "email must be 510 characters or fewer")
    String email,

    @NotBlank(message = "password is required")
    @Size(min = 4, max = 72, message = "password must be between 4 and 72 characters")
    String password,

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
