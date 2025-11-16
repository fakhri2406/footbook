package com.footbook.repository;

import com.footbook.domain.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {
    Optional<RevokedToken> findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
