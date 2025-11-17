package com.footbook.dto.request.branch;

import jakarta.validation.constraints.Email;

import java.math.BigDecimal;

public record UpdateBranchRequest(
    String name,
    String address,
    String googleMapsUrl,
    String operatingHoursStart,
    String operatingHoursEnd,
    String contactPhone,

    @Email(message = "Invalid email format")
    String contactEmail,

    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isActive
) {
}
