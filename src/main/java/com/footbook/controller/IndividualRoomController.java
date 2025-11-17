package com.footbook.controller;

import com.footbook.dto.request.room.CreateIndividualRoomRequest;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.dto.response.room.IndividualRoomDetailResponse;
import com.footbook.dto.response.room.IndividualRoomResponse;
import com.footbook.service.IndividualRoomService;
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
@RequestMapping("/api/v1/rooms/individual")
@Validated
@RequiredArgsConstructor
@Tag(name = "Individual Rooms", description = "Individual room booking management endpoints")
public class IndividualRoomController {
    private final IndividualRoomService individualRoomService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Create an individual room",
        description = "Creates a new individual room booking slot. The creator automatically becomes the first participant.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Room created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or time conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<IndividualRoomResponse> createRoom(@Valid @RequestBody CreateIndividualRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(individualRoomService.createRoom(request));
    }

    @GetMapping
    @Operation(
        summary = "Get all individual rooms",
        description = "Retrieves a paginated list of individual rooms with optional filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rooms retrieved successfully")
    })
    public ResponseEntity<Page<IndividualRoomResponse>> getAllRooms(
        @RequestParam(required = false) UUID branchId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
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
        return ResponseEntity.ok(individualRoomService.getAllRooms(branchId, startDate, endDate, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get individual room details",
        description = "Retrieves detailed information about a specific room including all participants"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room details retrieved successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Room not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<IndividualRoomDetailResponse> getRoomById(@PathVariable UUID id) {
        return ResponseEntity.ok(individualRoomService.getRoomById(id));
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Join an individual room",
        description = "Join an available individual room as a participant. Validates capacity and time conflicts.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined the room"),
        @ApiResponse(
            responseCode = "400",
            description = "Time conflict or already joined",
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
    public ResponseEntity<Void> joinRoom(@PathVariable UUID id) {
        individualRoomService.joinRoom(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/leave")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Leave an individual room",
        description = "Leave a room you've joined. Room owners cannot leave (must cancel instead).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully left the room"),
        @ApiResponse(
            responseCode = "400",
            description = "Not a participant or owner attempting to leave",
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
    public ResponseEntity<Void> leaveRoom(@PathVariable UUID id) {
        individualRoomService.leaveRoom(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Cancel an individual room",
        description = "Cancel a room (owner only). This removes the room and all participants.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Room cancelled successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Not the room owner",
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
        individualRoomService.cancelRoom(id);
        return ResponseEntity.noContent().build();
    }
}
