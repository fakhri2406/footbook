package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum NotificationType {
        ROOM_JOINED,
        ROOM_FULL,
        TEAM_INVITATION,
        BOOKING_CONFIRMATION,
        MATCH_REMINDER,
        TEAM_UPDATE
    }
}
