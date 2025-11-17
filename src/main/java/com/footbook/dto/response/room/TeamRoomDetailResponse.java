package com.footbook.dto.response.room;

import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.team.TeamDetailResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record TeamRoomDetailResponse(
    UUID id,
    BranchResponse branch,
    TeamDetailResponse creatorTeam,
    TeamDetailResponse opponentTeam,
    String scheduledDate,
    String startTime,
    String endTime,
    Integer requiredTeamSize,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
