package com.footbook.service.impl;

import com.footbook.domain.Notification;
import com.footbook.dto.response.notification.NotificationResponse;
import com.footbook.repository.NotificationRepository;
import com.footbook.repository.UserRepository;
import com.footbook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnreadNotifications() {
        UUID currentUserId = getCurrentUserId();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUserId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        UUID currentUserId = getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        UUID currentUserId = getCurrentUserId();

        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notification not found with ID: " + id));

        if (!notification.getUserId().equals(currentUserId)) {
            throw new IllegalStateException("You can only mark your own notifications as read");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.debug("Marked notification {} as read for user {}", id, currentUserId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        UUID currentUserId = getCurrentUserId();
        notificationRepository.markAllAsReadForUser(currentUserId);
        log.debug("Marked all notifications as read for user {}", currentUserId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        UUID currentUserId = getCurrentUserId();

        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notification not found with ID: " + id));

        if (!notification.getUserId().equals(currentUserId)) {
            throw new IllegalStateException("You can only delete your own notifications");
        }

        notificationRepository.delete(notification);
        log.debug("Deleted notification {} for user {}", id, currentUserId);
    }

    @Override
    @Transactional
    public void createNotification(UUID userId, String type, String title, String message, String entityType, UUID entityId) {
        try {
            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type);

            Notification notification = Notification.builder()
                .userId(userId)
                .type(notificationType)
                .title(title)
                .message(message)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .isRead(false)
                .build();

            notificationRepository.save(notification);
            log.info("Created notification for user {}: {} - {}", userId, title, message);
        } catch (IllegalArgumentException e) {
            log.error("Invalid notification type: {}", type);
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType().name(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getRelatedEntityType(),
            notification.getRelatedEntityId(),
            notification.getIsRead(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found"))
            .getId();
    }
}
