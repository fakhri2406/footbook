package com.footbook.controller;

import com.footbook.dto.request.team.AddMemberRequest;
import com.footbook.dto.request.team.CreateTeamRequest;
import com.footbook.dto.request.team.TransferCaptainRequest;
import com.footbook.dto.request.team.UpdateTeamRequest;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.dto.response.team.TeamDetailResponse;
import com.footbook.dto.response.team.TeamResponse;
import com.footbook.service.TeamService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@Validated
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Create a new team",
        description = "Creates a new team with the current user as captain. Captain is automatically added as the first member.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Team created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(request));
    }

    @GetMapping
    @Operation(
        summary = "Get all teams",
        description = "Retrieves a paginated list of active teams with optional name filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Teams retrieved successfully")
    })
    public ResponseEntity<Page<TeamResponse>> getAllTeams(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(teamService.getAllTeams(name, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get team details",
        description = "Retrieves detailed information about a specific team including all members"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team details retrieved successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Team not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TeamDetailResponse> getTeamById(@PathVariable UUID id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @GetMapping("/my-teams/captain")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get my teams as captain",
        description = "Retrieves all teams where the current user is the captain",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<TeamResponse>> getMyTeamsAsCaptain() {
        return ResponseEntity.ok(teamService.getMyTeamsAsCaptain());
    }

    @GetMapping("/my-teams/member")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get my teams as member",
        description = "Retrieves all teams where the current user is a member",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<TeamResponse>> getMyTeamsAsMember() {
        return ResponseEntity.ok(teamService.getMyTeamsAsMember());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Update team information",
        description = "Updates team information (captain only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team updated successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or not the captain",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Team not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<TeamResponse> updateTeam(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Add a member to the team",
        description = "Adds a new member to the team (captain only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Member added successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input, not the captain, or team is full",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Team or user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> addMember(
        @PathVariable UUID id,
        @Valid @RequestBody AddMemberRequest request) {
        teamService.addMember(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Remove a member from the team",
        description = "Removes a member from the team (captain only). Cannot remove the captain.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Member removed successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Not the captain or trying to remove captain",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Team or member not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> removeMember(
        @PathVariable UUID teamId,
        @PathVariable UUID userId) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transfer-captain")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Transfer captain role",
        description = "Transfers the captain role to another team member (captain only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Captain role transferred successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Not the captain or new captain is not a member",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Team or new captain not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> transferCaptain(
        @PathVariable UUID id,
        @Valid @RequestBody TransferCaptainRequest request) {
        teamService.transferCaptain(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Disband a team",
        description = "Disbands the team (captain only). This marks the team as disbanded.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Team disbanded successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Not the captain",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Team not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> disbandTeam(@PathVariable UUID id) {
        teamService.disbandTeam(id);
        return ResponseEntity.noContent().build();
    }
}
