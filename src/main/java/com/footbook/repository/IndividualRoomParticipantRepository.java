package com.footbook.repository;

import com.footbook.domain.IndividualRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IndividualRoomParticipantRepository extends JpaRepository<IndividualRoomParticipant, UUID> {
    List<IndividualRoomParticipant> findByRoomIdOrderByJoinedAtAsc(UUID roomId);

    long countByRoomId(UUID roomId);

    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

    Optional<IndividualRoomParticipant> findByRoomIdAndUserId(UUID roomId, UUID userId);

    void deleteByRoomIdAndUserId(UUID roomId, UUID userId);

    @Query("SELECT p FROM IndividualRoomParticipant p " +
        "JOIN IndividualRoom r ON p.roomId = r.id " +
        "WHERE p.userId = :userId " +
        "AND r.status <> 'CANCELLED' " +
        "AND (r.scheduledDate > :today OR (r.scheduledDate = :today AND r.startTime >= :currentTime)) " +
        "ORDER BY r.scheduledDate ASC, r.startTime ASC")
    List<IndividualRoomParticipant> findUpcomingParticipationsByUser(
        @Param("userId") UUID userId,
        @Param("today") LocalDate today,
        @Param("currentTime") LocalTime currentTime
    );

    @Query("SELECT p FROM IndividualRoomParticipant p " +
        "JOIN IndividualRoom r ON p.roomId = r.id " +
        "WHERE p.userId = :userId " +
        "AND (r.scheduledDate < :today OR (r.scheduledDate = :today AND r.startTime < :currentTime)) " +
        "ORDER BY r.scheduledDate DESC, r.startTime DESC")
    List<IndividualRoomParticipant> findPastParticipationsByUser(
        @Param("userId") UUID userId,
        @Param("today") LocalDate today,
        @Param("currentTime") LocalTime currentTime
    );

    List<IndividualRoomParticipant> findByRoomIdIn(List<UUID> roomIds);
}
