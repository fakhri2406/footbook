package com.footbook.repository;

import com.footbook.domain.IndividualRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface IndividualRoomRepository extends JpaRepository<IndividualRoom, UUID> {
    @Query("SELECT r FROM IndividualRoom r WHERE " +
        "(:branchId IS NULL OR r.branchId = :branchId) AND " +
        "(:startDate IS NULL OR r.scheduledDate >= :startDate) AND " +
        "(:endDate IS NULL OR r.scheduledDate <= :endDate) AND " +
        "(:status IS NULL OR r.status = :status) AND " +
        "r.status <> 'CANCELLED'")
    Page<IndividualRoom> findRoomsWithFilters(
        @Param("branchId") UUID branchId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") IndividualRoom.RoomStatus status,
        Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
        "FROM IndividualRoom r " +
        "JOIN IndividualRoomParticipant p ON r.id = p.roomId " +
        "WHERE p.userId = :userId " +
        "AND r.scheduledDate = :date " +
        "AND r.status <> 'CANCELLED' " +
        "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    boolean hasUserConflict(
        @Param("userId") UUID userId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
        "FROM IndividualRoom r " +
        "JOIN IndividualRoomParticipant p ON r.id = p.roomId " +
        "WHERE p.userId = :userId " +
        "AND r.id <> :excludeRoomId " +
        "AND r.scheduledDate = :date " +
        "AND r.status <> 'CANCELLED' " +
        "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    boolean hasUserConflictExcludingRoom(
        @Param("userId") UUID userId,
        @Param("excludeRoomId") UUID excludeRoomId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    Optional<IndividualRoom> findByIdAndStatusNot(UUID id, IndividualRoom.RoomStatus status);

    List<IndividualRoom> findByOwnerIdOrderByScheduledDateDescStartTimeDesc(UUID ownerId);

    @Query("SELECT r FROM IndividualRoom r WHERE r.ownerId = :ownerId " +
        "AND r.status <> 'CANCELLED' " +
        "AND (r.scheduledDate > :today OR (r.scheduledDate = :today AND r.startTime >= :currentTime)) " +
        "ORDER BY r.scheduledDate ASC, r.startTime ASC")
    List<IndividualRoom> findUpcomingRoomsByOwner(
        @Param("ownerId") UUID ownerId,
        @Param("today") LocalDate today,
        @Param("currentTime") LocalTime currentTime
    );
}
