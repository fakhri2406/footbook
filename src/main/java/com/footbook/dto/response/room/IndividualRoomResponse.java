package com.footbook.dto.response.room;

import com.footbook.dto.response.branch.BranchResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record IndividualRoomResponse(
    UUID id,
    BranchResponse branch,
    ParticipantSummary owner,
    String scheduledDate,
    String startTime,
    String endTime,
    Integer totalSlots,
    Integer filledSlots,
    Integer availableSlots,
    String notes,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record ParticipantSummary(
        UUID id,
        String firstName,
        String lastName,
        String profilePictureUrl
    ) {
    }
}
