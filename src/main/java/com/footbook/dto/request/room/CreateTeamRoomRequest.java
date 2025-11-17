package com.footbook.dto.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTeamRoomRequest(
    @NotNull(message = "Branch ID is required")
    UUID branchId,

    @NotNull(message = "Team ID is required")
    UUID teamId,

    @NotBlank(message = "Scheduled date is required (YYYY-MM-DD format)")
    String scheduledDate,

    @NotBlank(message = "Start time is required (HH:mm format)")
    String startTime,

    @NotBlank(message = "End time is required (HH:mm format)")
    String endTime
) {
}
