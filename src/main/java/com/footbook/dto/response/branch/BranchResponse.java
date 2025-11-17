package com.footbook.dto.response.branch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    String name,
    String address,
    String googleMapsUrl,
    String operatingHoursStart,
    String operatingHoursEnd,
    String contactPhone,
    String contactEmail,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
