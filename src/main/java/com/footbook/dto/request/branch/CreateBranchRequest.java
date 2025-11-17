package com.footbook.dto.request.branch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateBranchRequest(
    @NotBlank(message = "Branch name is required")
    String name,

    @NotBlank(message = "Address is required")
    String address,

    String googleMapsUrl,

    @NotBlank(message = "Operating hours start time is required (HH:mm format)")
    String operatingHoursStart,

    @NotBlank(message = "Operating hours end time is required (HH:mm format)")
    String operatingHoursEnd,

    String contactPhone,

    @Email(message = "Invalid email format")
    String contactEmail,

    BigDecimal latitude,

    BigDecimal longitude
) {
}
