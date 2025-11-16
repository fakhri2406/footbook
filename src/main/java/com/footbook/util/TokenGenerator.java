package com.footbook.util;

import com.footbook.config.jwt.JwtProperties;
import com.footbook.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .setIssuer(jwtProperties.getIssuer())
            .setAudience(jwtProperties.getAudience())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(jwtProperties.getAccessTokenValiditySeconds())))
            .claim("userId", user.getId().toString())
            .claim("email", user.getEmail())
            .claim("firstName", user.getFirstName())
            .claim("lastName", user.getLastName())
            .claim("profilePictureUrl", user.getProfilePictureUrl())
            .claim("isActive", user.getIsActive())
            .claim("isVerified", user.getIsVerified())
            .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
            .compact();
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public int generateCode() {
        return 100000 + RANDOM.nextInt(900000);
    }
}
