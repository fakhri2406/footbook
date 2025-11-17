package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "branches")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "google_maps_url", columnDefinition = "TEXT")
    private String googleMapsUrl;

    @Column(name = "operating_hours_start", nullable = false)
    private LocalTime operatingHoursStart;

    @Column(name = "operating_hours_end", nullable = false)
    private LocalTime operatingHoursEnd;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
