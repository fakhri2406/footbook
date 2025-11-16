package com.footbook.dto.response.profile;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    LocalDateTime dateOfBirth,
    String profilePictureUrl,
    Boolean isPremium,
    Boolean verified,
    String role,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
