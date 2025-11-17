package com.footbook.dto.response.team;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TeamDetailResponse(
    UUID id,
    String name,
    String description,
    String logoUrl,
    CaptainInfo captain,
    Integer rosterSize,
    Integer currentMemberCount,
    Integer availableSlots,
    String status,
    List<MemberInfo> members,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record CaptainInfo(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl
    ) {
    }

    public record MemberInfo(
        UUID id,
        String firstName,
        String lastName,
        String profilePictureUrl,
        LocalDateTime joinedAt
    ) {
    }
}
