package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "teams")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "captain_id", nullable = false)
    private UUID captainId;

    @Column(name = "roster_size", nullable = false)
    private Integer rosterSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TeamStatus status;

    public enum TeamStatus {
        ACTIVE,
        DISBANDED
    }
}
