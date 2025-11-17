package com.footbook.dto.request.team;

public record UpdateTeamRequest(
    String name,
    String description,
    String logoUrl
) {
}
