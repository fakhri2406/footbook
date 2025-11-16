package com.footbook.service;

import com.footbook.dto.request.auth.*;
import com.footbook.dto.response.auth.AuthResponse;
import com.footbook.dto.response.auth.MeResponse;

/**
 * Service interface for authentication and user account management operations.
 * Handles user registration, email verification, login, token management, and password reset.
 */
public interface AuthService {
    /**
     * Registers a new user account and sends verification email.
     *
     * @param request the signup request containing user details
     * @throws IllegalArgumentException if email already exists
     */
    void signup(SignupRequest request);

    /**
     * Verifies user's email address using verification code and completes registration.
     *
     * @param request the verification request containing email and code
     * @return authentication response with access and refresh tokens
     * @throws IllegalArgumentException if verification code is invalid or expired
     */
    AuthResponse verify(VerifyRequest request);

    /**
     * Resends the email verification code to the user.
     *
     * @param request the resend request containing user email
     * @throws NoSuchElementException if user not found
     * @throws IllegalStateException  if user is already verified
     */
    void resend(ResendVerificationRequest request);

    /**
     * Authenticates user and generates access and refresh tokens.
     *
     * @param request the login request containing email and password
     * @return authentication response with access and refresh tokens
     * @throws NoSuchElementException   if user not found
     * @throws IllegalArgumentException if password is incorrect or user not verified
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return authentication response with new access and refresh tokens
     * @throws NoSuchElementException   if refresh token not found
     * @throws IllegalArgumentException if refresh token is expired or revoked
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Revokes the provided access token, logging out the user.
     *
     * @param accessToken the JWT access token to revoke
     */
    void logout(String accessToken);

    /**
     * Extracts and revokes the access token from Authorization header.
     *
     * @param authorizationHeader the Authorization header value (Bearer token)
     */
    void logoutFromHeader(String authorizationHeader);

    /**
     * Retrieves the authenticated user's profile information.
     *
     * @return the user's profile details
     * @throws NoSuchElementException if authenticated user not found
     */
    MeResponse me();

    /**
     * Initiates password reset process by sending reset code to user's email.
     *
     * @param request the password reset request containing user email
     * @throws NoSuchElementException if user not found
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Confirms password reset using reset code and sets new password.
     *
     * @param request the confirm reset request containing email, code, and new password
     * @throws IllegalArgumentException if reset code is invalid or expired
     */
    void confirmResetPassword(ConfirmResetPasswordRequest request);
}
