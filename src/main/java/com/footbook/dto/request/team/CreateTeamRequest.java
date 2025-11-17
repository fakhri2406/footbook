package com.footbook.dto.request.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTeamRequest(
    @NotBlank(message = "Team name is required")
    String name,

    String description,

    String logoUrl,

    @NotNull(message = "Roster size is required")
    @Min(value = 2, message = "Roster size must be at least 2")
    Integer rosterSize
) {
}
