package com.footbook.service;

import com.footbook.dto.request.room.CreateIndividualRoomRequest;
import com.footbook.dto.response.room.IndividualRoomDetailResponse;
import com.footbook.dto.response.room.IndividualRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for individual room management operations.
 */
public interface IndividualRoomService {
    /**
     * Create a new individual room
     *
     * @param request room creation request
     * @return created room details
     * @throws IllegalArgumentException if branch not found, time conflicts, or invalid time range
     */
    IndividualRoomResponse createRoom(CreateIndividualRoomRequest request);

    /**
     * Get all rooms with filters
     *
     * @param branchId  optional branch filter
     * @param startDate optional start date filter
     * @param endDate   optional end date filter
     * @param statusStr optional status filter
     * @param pageable  pagination information
     * @return paginated list of rooms
     */
    Page<IndividualRoomResponse> getAllRooms(UUID branchId, LocalDate startDate, LocalDate endDate, String statusStr, Pageable pageable);

    /**
     * Get room details by ID
     *
     * @param id room ID
     * @return room details with participant list
     * @throws java.util.NoSuchElementException if room not found
     */
    IndividualRoomDetailResponse getRoomById(UUID id);

    /**
     * Join a room as a participant
     *
     * @param roomId room ID
     * @throws java.util.NoSuchElementException if room not found
     * @throws IllegalStateException            if room is full or user already joined
     * @throws IllegalArgumentException         if user has time conflict
     */
    void joinRoom(UUID roomId);

    /**
     * Leave a room (participant only, not owner)
     *
     * @param roomId room ID
     * @throws java.util.NoSuchElementException if room not found or user not a participant
     * @throws IllegalStateException            if user is the owner
     */
    void leaveRoom(UUID roomId);

    /**
     * Cancel a room (owner only)
     *
     * @param roomId room ID
     * @throws java.util.NoSuchElementException if room not found
     * @throws IllegalStateException            if user is not the owner
     */
    void cancelRoom(UUID roomId);
}
