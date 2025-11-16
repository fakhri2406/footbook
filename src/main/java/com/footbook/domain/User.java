package com.footbook.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "verification_code")
    private Integer verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "verification_attempts", nullable = false)
    private Integer verificationAttempts;

    @Column(name = "verification_locked_until")
    private LocalDateTime verificationLockedUntil;

    @Column(name = "verification_last_sent_at")
    private LocalDateTime verificationLastSentAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(name = "pending_email_code")
    private Integer pendingEmailCode;

    @Column(name = "pending_email_expires_at")
    private LocalDateTime pendingEmailExpiresAt;

    @Column(name = "pending_email_attempts", nullable = false)
    private Integer pendingEmailAttempts;

    @Column(name = "pending_email_locked_until")
    private LocalDateTime pendingEmailLockedUntil;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "password_reset_attempts", nullable = false)
    private Integer passwordResetAttempts;

    @Column(name = "password_reset_locked_until")
    private LocalDateTime passwordResetLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
