package com.footbook.controller;

import com.footbook.dto.request.room.CreateTeamRoomRequest;
import com.footbook.dto.request.room.JoinTeamRoomRequest;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.dto.response.room.TeamRoomDetailResponse;
import com.footbook.dto.response.room.TeamRoomResponse;
import com.footbook.service.TeamRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms/team")
@Validated
@RequiredArgsConstructor
@Tag(name = "Team Rooms", description = "Team vs Team room booking management endpoints")
public class TeamRoomController {
    private final TeamRoomService teamRoomService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Create a team room",
        description = "Creates a new team room booking (captain only). Team must have full roster. Validates conflicts for all team members.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Team room created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input, not captain, team not full, or time conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TeamRoomResponse> createRoom(@Valid @RequestBody CreateTeamRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamRoomService.createRoom(request));
    }

    @GetMapping
    @Operation(
        summary = "Get all team rooms",
        description = "Retrieves a paginated list of team rooms with optional filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rooms retrieved successfully")
    })
    public ResponseEntity<Page<TeamRoomResponse>> getAllRooms(
        @RequestParam(required = false) UUID branchId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) Integer teamSize,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "scheduledDate,startTime") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

        String[] sortFields = sortBy.split(",");
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
            ? Sort.by(sortFields).descending()
            : Sort.by(sortFields).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(teamRoomService.getAllRooms(branchId, startDate, endDate, teamSize, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get team room details",
        description = "Retrieves detailed information about a specific team room including both teams' details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room details retrieved successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TeamRoomDetailResponse> getRoomById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamRoomService.getRoomById(id));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Join a team room as opponent",
        description = "Join an available team room with your team (captain only). Team must have matching size and full roster. Validates conflicts for all team members.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined the room"),
        @ApiResponse(
            responseCode = "400",
            description = "Not captain, team size mismatch, team not full, or time conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Room or team not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> joinRoom(
        @PathVariable UUID id,
        @Valid @RequestBody JoinTeamRoomRequest request) {
        teamRoomService.joinRoom(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Cancel a team room",
        description = "Cancel a team room (creator captain only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Room cancelled successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Not the creator captain",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> cancelRoom(@PathVariable UUID id) {
        teamRoomService.cancelRoom(id);
        return ResponseEntity.noContent().build();
    }
}
