package com.footbook.controller;

import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.dto.response.notification.NotificationResponse;
import com.footbook.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management endpoints")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get my notifications",
        description = "Retrieves a paginated list of notifications for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get unread notifications",
        description = "Retrieves all unread notifications for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications());
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Get unread notification count",
        description = "Returns the count of unread notifications for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount()));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Mark notification as read",
        description = "Marks a specific notification as read",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications as read for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    @Operation(
        summary = "Delete notification",
        description = "Deletes a specific notification",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
