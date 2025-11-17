package com.footbook.service;

import com.footbook.dto.response.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for notification management operations.
 */
public interface NotificationService {
    /**
     * Get all notifications for the current user with pagination
     *
     * @param pageable pagination information
     * @return paginated list of notifications
     */
    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    /**
     * Get unread notifications for the current user
     *
     * @return list of unread notifications
     */
    List<NotificationResponse> getMyUnreadNotifications();

    /**
     * Get count of unread notifications for the current user
     *
     * @return count of unread notifications
     */
    long getUnreadCount();

    /**
     * Mark a notification as read
     *
     * @param id notification ID
     * @throws java.util.NoSuchElementException if notification not found
     * @throws IllegalStateException            if notification doesn't belong to current user
     */
    void markAsRead(UUID id);

    /**
     * Mark all notifications as read for the current user
     */
    void markAllAsRead();

    /**
     * Delete a notification
     *
     * @param id notification ID
     * @throws java.util.NoSuchElementException if notification not found
     * @throws IllegalStateException            if notification doesn't belong to current user
     */
    void deleteNotification(UUID id);

    /**
     * Create a notification for a user
     * (Internal use by event listeners)
     *
     * @param userId     recipient user ID
     * @param type       notification type
     * @param title      notification title
     * @param message    notification message
     * @param entityType related entity type
     * @param entityId   related entity ID
     */
    void createNotification(UUID userId, String type, String title, String message, String entityType, UUID entityId);
}
