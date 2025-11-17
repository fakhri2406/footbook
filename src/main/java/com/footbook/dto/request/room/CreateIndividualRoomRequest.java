package com.footbook.dto.request.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateIndividualRoomRequest(
    @NotNull(message = "Branch ID is required")
    UUID branchId,

    @NotBlank(message = "Scheduled date is required (YYYY-MM-DD format)")
    String scheduledDate,

    @NotBlank(message = "Start time is required (HH:mm format)")
    String startTime,

    @NotBlank(message = "End time is required (HH:mm format)")
    String endTime,

    @NotNull(message = "Total slots is required")
    @Min(value = 2, message = "Total slots must be at least 2")
    Integer totalSlots,

    String notes
) {
}
