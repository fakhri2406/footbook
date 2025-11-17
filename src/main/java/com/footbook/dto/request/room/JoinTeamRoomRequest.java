package com.footbook.dto.request.room;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinTeamRoomRequest(
    @NotNull(message = "Team ID is required")
    UUID teamId
) {
}
