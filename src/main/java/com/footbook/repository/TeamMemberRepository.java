package com.footbook.repository;

import com.footbook.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamIdOrderByJoinedAtAsc(UUID teamId);

    long countByTeamId(UUID teamId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

    @Query("SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId")
    List<UUID> findTeamIdsByUserId(@Param("userId") UUID userId);

    List<TeamMember> findByTeamIdIn(List<UUID> teamIds);

    @Query("SELECT tm.userId FROM TeamMember tm WHERE tm.teamId = :teamId")
    List<UUID> findUserIdsByTeamId(@Param("teamId") UUID teamId);
}
