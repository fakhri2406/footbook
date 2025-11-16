package com.footbook.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email
) {
}
