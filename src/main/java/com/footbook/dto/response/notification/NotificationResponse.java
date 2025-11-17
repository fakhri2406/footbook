package com.footbook.dto.response.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String type,
    String title,
    String message,
    String relatedEntityType,
    UUID relatedEntityId,
    Boolean isRead,
    LocalDateTime readAt,
    LocalDateTime createdAt
) {
}
