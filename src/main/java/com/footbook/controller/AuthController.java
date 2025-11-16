package com.footbook.controller;

import com.footbook.dto.request.auth.*;
import com.footbook.dto.response.auth.AuthResponse;
import com.footbook.dto.response.auth.MeResponse;
import com.footbook.dto.response.auth.PasswordStrengthResponse;
import com.footbook.dto.response.error.ErrorResponse;
import com.footbook.service.AuthService;
import com.footbook.util.PasswordStrengthEstimator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user account management endpoints")
public class AuthController {
    private final AuthService authService;
    private final PasswordStrengthEstimator passwordStrengthEstimator;

    @PostMapping("/signup")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and sends a verification email with a 6-digit code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or email already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify")
    @Operation(
        summary = "Verify email address",
        description = "Verifies user's email using the 6-digit code and returns JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification code",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/resend")
    @Operation(
        summary = "Resend verification email",
        description = "Sends a new verification code to the user's email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "User not found or already verified",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resend(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user and returns access and refresh tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid credentials or email not verified",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generates new access and refresh tokens using a valid refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired refresh token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Revokes the user's access token",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logoutFromHeader(authorization);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user info",
        description = "Retrieves the authenticated user's profile information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Request password reset",
        description = "Sends a password reset code to the user's email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset code sent successfully"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-reset-password")
    @Operation(
        summary = "Confirm password reset",
        description = "Resets the user's password using the reset code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired reset code",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        authService.confirmResetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-password-strength")
    @Operation(
        summary = "Check password strength",
        description = "Evaluates password strength in real-time for mobile frontend. Returns score, level, suggestions, and estimated crack time. No authentication required - designed for use during signup/password change."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password strength evaluated successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<PasswordStrengthResponse> checkPasswordStrength(@Valid @RequestBody PasswordStrengthRequest request) {
        PasswordStrengthEstimator.PasswordStrengthResult result = passwordStrengthEstimator.estimateStrength(request.password());

        PasswordStrengthResponse response = new PasswordStrengthResponse(
            result.score(),
            result.level(),
            result.message(),
            result.suggestions(),
            result.getFormattedCrackTime()
        );

        return ResponseEntity.ok(response);
    }
}
