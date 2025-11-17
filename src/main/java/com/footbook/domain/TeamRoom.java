package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "team_rooms")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "creator_team_id", nullable = false)
    private UUID creatorTeamId;

    @Column(name = "opponent_team_id")
    private UUID opponentTeamId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "required_team_size", nullable = false)
    private Integer requiredTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TeamRoomStatus status;

    public enum TeamRoomStatus {
        OPEN,
        MATCHED,
        CANCELLED
    }
}
