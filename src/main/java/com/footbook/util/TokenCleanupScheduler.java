package com.footbook.util;

import com.footbook.repository.RefreshTokenRepository;
import com.footbook.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            refreshTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up expired refresh tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired refresh tokens", e);
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            revokedTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up expired revoked tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired revoked tokens", e);
        }
    }
}
