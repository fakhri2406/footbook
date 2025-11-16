package com.footbook.dto.request.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PasswordStrengthRequest(
    @NotNull(message = "Password is required")
    @Size(max = 128, message = "Password must not exceed 128 characters")
    String password
) {
}
