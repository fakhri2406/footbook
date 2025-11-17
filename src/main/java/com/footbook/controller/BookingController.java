package com.footbook.controller;

import com.footbook.dto.response.booking.BookingResponse;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "User booking management endpoints")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get all my bookings",
        description = "Retrieves all bookings (individual and team rooms) for the current user, including both upcoming and past bookings",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/my-bookings/upcoming")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get my upcoming bookings",
        description = "Retrieves all upcoming bookings for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming bookings retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<BookingResponse>> getMyUpcomingBookings() {
        return ResponseEntity.ok(bookingService.getMyUpcomingBookings());
    }

    @GetMapping("/my-bookings/past")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get my past bookings",
        description = "Retrieves all past bookings for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Past bookings retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<BookingResponse>> getMyPastBookings() {
        return ResponseEntity.ok(bookingService.getMyPastBookings());
    }
}
