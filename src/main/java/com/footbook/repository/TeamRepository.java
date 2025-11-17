package com.footbook.repository;

import com.footbook.domain.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    @Query("SELECT t FROM Team t WHERE t.status = 'ACTIVE' " +
        "AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Team> findActiveTeams(@Param("name") String name, Pageable pageable);

    Optional<Team> findByIdAndStatus(UUID id, Team.TeamStatus status);

    List<Team> findByCaptainIdAndStatusOrderByCreatedAtDesc(UUID captainId, Team.TeamStatus status);

    @Query("SELECT t FROM Team t " +
        "JOIN TeamMember tm ON t.id = tm.teamId " +
        "WHERE tm.userId = :userId AND t.status = 'ACTIVE' " +
        "ORDER BY t.createdAt DESC")
    List<Team> findTeamsByMember(@Param("userId") UUID userId);

    boolean existsByIdAndStatus(UUID id, Team.TeamStatus status);
}
