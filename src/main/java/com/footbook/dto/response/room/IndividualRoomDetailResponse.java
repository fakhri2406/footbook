package com.footbook.dto.response.room;

import com.footbook.dto.response.branch.BranchResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record IndividualRoomDetailResponse(
    UUID id,
    BranchResponse branch,
    ParticipantInfo owner,
    String scheduledDate,
    String startTime,
    String endTime,
    Integer totalSlots,
    Integer filledSlots,
    Integer availableSlots,
    String notes,
    String status,
    List<ParticipantInfo> participants,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record ParticipantInfo(
        UUID id,
        String firstName,
        String lastName,
        String profilePictureUrl,
        LocalDateTime joinedAt
    ) {
    }
}
