package com.footbook.dto.response.team;

import java.time.LocalDateTime;
import java.util.UUID;

public record TeamResponse(
    UUID id,
    String name,
    String description,
    String logoUrl,
    CaptainSummary captain,
    Integer rosterSize,
    Integer currentMemberCount,
    Integer availableSlots,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record CaptainSummary(
        UUID id,
        String firstName,
        String lastName,
        String profilePictureUrl
    ) {
    }
}
