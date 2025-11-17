package com.footbook.repository;

import com.footbook.domain.TeamRoom;
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
public interface TeamRoomRepository extends JpaRepository<TeamRoom, UUID> {
    @Query("SELECT r FROM TeamRoom r WHERE " +
        "(:branchId IS NULL OR r.branchId = :branchId) AND " +
        "(:startDate IS NULL OR r.scheduledDate >= :startDate) AND " +
        "(:endDate IS NULL OR r.scheduledDate <= :endDate) AND " +
        "(:teamSize IS NULL OR r.requiredTeamSize = :teamSize) AND " +
        "(:status IS NULL OR r.status = :status) AND " +
        "r.status <> 'CANCELLED'")
    Page<TeamRoom> findRoomsWithFilters(
        @Param("branchId") UUID branchId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("teamSize") Integer teamSize,
        @Param("status") TeamRoom.TeamRoomStatus status,
        Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
        "FROM TeamRoom r " +
        "WHERE (r.creatorTeamId = :teamId OR r.opponentTeamId = :teamId) " +
        "AND r.scheduledDate = :date " +
        "AND r.status <> 'CANCELLED' " +
        "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    boolean hasTeamConflict(
        @Param("teamId") UUID teamId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
        "FROM IndividualRoom r " +
        "JOIN IndividualRoomParticipant p ON r.id = p.roomId " +
        "WHERE p.userId IN :userIds " +
        "AND r.scheduledDate = :date " +
        "AND r.status <> 'CANCELLED' " +
        "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    boolean hasTeamMembersIndividualConflict(
        @Param("userIds") List<UUID> userIds,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    Optional<TeamRoom> findByIdAndStatusNot(UUID id, TeamRoom.TeamRoomStatus status);

    List<TeamRoom> findByCreatorTeamIdOrderByScheduledDateDescStartTimeDesc(UUID teamId);

    List<TeamRoom> findByOpponentTeamIdOrderByScheduledDateDescStartTimeDesc(UUID teamId);

    @Query("SELECT r FROM TeamRoom r WHERE " +
        "(r.creatorTeamId = :teamId OR r.opponentTeamId = :teamId) " +
        "AND r.status <> 'CANCELLED' " +
        "AND (r.scheduledDate > :today OR (r.scheduledDate = :today AND r.startTime >= :currentTime)) " +
        "ORDER BY r.scheduledDate ASC, r.startTime ASC")
    List<TeamRoom> findUpcomingRoomsByTeam(
        @Param("teamId") UUID teamId,
        @Param("today") LocalDate today,
        @Param("currentTime") LocalTime currentTime
    );
}
