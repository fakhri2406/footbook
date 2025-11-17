package com.footbook.repository;

import com.footbook.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);
}
