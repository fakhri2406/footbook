package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
