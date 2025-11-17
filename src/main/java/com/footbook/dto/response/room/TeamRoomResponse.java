package com.footbook.dto.response.room;

import com.footbook.dto.response.branch.BranchResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record TeamRoomResponse(
    UUID id,
    BranchResponse branch,
    TeamSummary creatorTeam,
    TeamSummary opponentTeam,
    String scheduledDate,
    String startTime,
    String endTime,
    Integer requiredTeamSize,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record TeamSummary(
        UUID id,
        String name,
        String logoUrl,
        Integer rosterSize,
        Integer currentMemberCount
    ) {
    }
}
