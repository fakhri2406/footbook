package com.footbook.controller;

import com.footbook.dto.request.branch.CreateBranchRequest;
import com.footbook.dto.request.branch.UpdateBranchRequest;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.service.BranchService;
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
@RequestMapping("/api/v1/branches")
@Validated
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Stadium branch management endpoints")
public class BranchController {
    private final BranchService branchService;

    @GetMapping
    @Operation(
        summary = "Get all branches",
        description = "Retrieves a paginated list of active stadium branches with optional name filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Branches retrieved successfully")
    })
    public ResponseEntity<Page<BranchResponse>> getAllBranches(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(branchService.getAllBranches(name, pageable));
    }

    @GetMapping("/list")
    @Operation(
        summary = "Get all active branches",
        description = "Retrieves a complete list of all active branches without pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Branches retrieved successfully")
    })
    public ResponseEntity<List<BranchResponse>> getAllActiveBranches() {
        return ResponseEntity.ok(branchService.getAllActiveBranches());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get branch by ID",
        description = "Retrieves detailed information about a specific branch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Branch retrieved successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "Branch not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable UUID id) {
        return ResponseEntity.ok(branchService.getBranchById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
        summary = "Create a new branch",
        description = "Creates a new stadium branch (ADMIN only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Branch created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin access required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BranchResponse> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
        summary = "Update a branch",
        description = "Updates an existing stadium branch (ADMIN only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Branch updated successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin access required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Branch not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BranchResponse> updateBranch(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(branchService.updateBranch(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
        summary = "Delete a branch",
        description = "Soft deletes a stadium branch (ADMIN only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Branch deleted successfully"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin access required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Branch not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
