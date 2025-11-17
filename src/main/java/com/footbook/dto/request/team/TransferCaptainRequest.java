package com.footbook.dto.request.team;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferCaptainRequest(
    @NotNull(message = "New captain user ID is required")
    UUID newCaptainId
) {
}
