package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "individual_room_participants")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualRoomParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
