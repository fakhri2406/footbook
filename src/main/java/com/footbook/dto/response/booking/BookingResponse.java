package com.footbook.dto.response.booking;

import com.footbook.dto.response.branch.BranchResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    String bookingType,
    BranchResponse branch,
    String scheduledDate,
    String startTime,
    String endTime,
    BookingDetails details,
    String status,
    LocalDateTime createdAt
) {
    public record BookingDetails(
        Integer totalSlots,
        Integer filledSlots,
        String ownerName,

        String creatorTeamName,
        String opponentTeamName,
        Integer requiredTeamSize
    ) {
    }
}
