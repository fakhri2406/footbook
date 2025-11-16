package com.footbook.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email,

    @NotNull(message = "Verification code is required")
    Integer code
) {
}
