package com.footbook.service;

import com.footbook.dto.request.team.AddMemberRequest;
import com.footbook.dto.request.team.CreateTeamRequest;
import com.footbook.dto.request.team.TransferCaptainRequest;
import com.footbook.dto.request.team.UpdateTeamRequest;
import com.footbook.dto.response.team.TeamDetailResponse;
import com.footbook.dto.response.team.TeamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for team management operations.
 */
public interface TeamService {
    /**
     * Create a new team
     *
     * @param request team creation request
     * @return created team details
     */
    TeamResponse createTeam(CreateTeamRequest request);

    /**
     * Get all active teams with pagination and filtering
     *
     * @param name     optional name filter
     * @param pageable pagination information
     * @return paginated list of teams
     */
    Page<TeamResponse> getAllTeams(String name, Pageable pageable);

    /**
     * Get team details by ID
     *
     * @param id team ID
     * @return team details with member list
     * @throws java.util.NoSuchElementException if team not found
     */
    TeamDetailResponse getTeamById(UUID id);

    /**
     * Get teams where current user is captain
     *
     * @return list of teams
     */
    List<TeamResponse> getMyTeamsAsCaptain();

    /**
     * Get teams where current user is a member
     *
     * @return list of teams
     */
    List<TeamResponse> getMyTeamsAsMember();

    /**
     * Update team information (captain only)
     *
     * @param id      team ID
     * @param request update request
     * @return updated team details
     * @throws java.util.NoSuchElementException if team not found
     * @throws IllegalStateException            if not the captain
     */
    TeamResponse updateTeam(UUID id, UpdateTeamRequest request);

    /**
     * Add a member to the team (captain only)
     *
     * @param id      team ID
     * @param request add member request
     * @throws java.util.NoSuchElementException if team or user not found
     * @throws IllegalStateException            if not the captain or team is full
     * @throws IllegalArgumentException         if user is already a member
     */
    void addMember(UUID id, AddMemberRequest request);

    /**
     * Remove a member from the team (captain only)
     *
     * @param teamId team ID
     * @param userId user ID to remove
     * @throws java.util.NoSuchElementException if team or member not found
     * @throws IllegalStateException            if not the captain or trying to remove captain
     */
    void removeMember(UUID teamId, UUID userId);

    /**
     * Transfer captain role to another member (captain only)
     *
     * @param id      team ID
     * @param request transfer request
     * @throws java.util.NoSuchElementException if team or new captain not found
     * @throws IllegalStateException            if not the captain
     * @throws IllegalArgumentException         if new captain is not a member
     */
    void transferCaptain(UUID id, TransferCaptainRequest request);

    /**
     * Disband a team (captain only)
     *
     * @param id team ID
     * @throws java.util.NoSuchElementException if team not found
     * @throws IllegalStateException            if not the captain
     */
    void disbandTeam(UUID id);
}
