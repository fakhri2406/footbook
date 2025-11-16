package com.footbook.dto.response.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {
}
