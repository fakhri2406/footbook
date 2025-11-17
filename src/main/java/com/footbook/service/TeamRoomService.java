package com.footbook.service;

import com.footbook.dto.request.room.CreateTeamRoomRequest;
import com.footbook.dto.request.room.JoinTeamRoomRequest;
import com.footbook.dto.response.room.TeamRoomDetailResponse;
import com.footbook.dto.response.room.TeamRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for team room management operations.
 */
public interface TeamRoomService {
    /**
     * Create a new team room (captain only)
     *
     * @param request room creation request
     * @return created room details
     * @throws IllegalArgumentException if branch not found, not captain, time conflicts, or invalid time range
     * @throws IllegalStateException    if team doesn't have enough members
     */
    TeamRoomResponse createRoom(CreateTeamRoomRequest request);

    /**
     * Get all team rooms with filters
     *
     * @param branchId  optional branch filter
     * @param startDate optional start date filter
     * @param endDate   optional end date filter
     * @param teamSize  optional team size filter
     * @param statusStr optional status filter
     * @param pageable  pagination information
     * @return paginated list of rooms
     */
    Page<TeamRoomResponse> getAllRooms(UUID branchId, LocalDate startDate, LocalDate endDate, Integer teamSize, String statusStr, Pageable pageable);

    /**
     * Get room details by ID
     *
     * @param id room ID
     * @return room details with both teams' information
     * @throws java.util.NoSuchElementException if room not found
     */
    TeamRoomDetailResponse getRoomById(UUID id);

    /**
     * Join a team room as opponent (captain only)
     *
     * @param roomId  room ID
     * @param request join request with team ID
     * @throws java.util.NoSuchElementException if room or team not found
     * @throws IllegalStateException            if room is full, not captain, or team size mismatch
     * @throws IllegalArgumentException         if any team member has time conflict
     */
    void joinRoom(UUID roomId, JoinTeamRoomRequest request);

    /**
     * Cancel a team room (creator captain only, before match)
     *
     * @param roomId room ID
     * @throws java.util.NoSuchElementException if room not found
     * @throws IllegalStateException            if not the creator captain or match already set
     */
    void cancelRoom(UUID roomId);
}
