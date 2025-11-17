package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "individual_rooms")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;

    public enum RoomStatus {
        OPEN,
        FULL,
        CANCELLED
    }
}
