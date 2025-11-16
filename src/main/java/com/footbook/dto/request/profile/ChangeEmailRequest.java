package com.footbook.dto.request.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "New email is required")
    String newEmail
) {
}
