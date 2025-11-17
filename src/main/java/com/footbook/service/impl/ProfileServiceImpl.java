package com.footbook.service.impl;

import com.footbook.config.email.EmailProperties;
import com.footbook.domain.Role;
import com.footbook.domain.User;
import com.footbook.dto.request.profile.ChangeEmailRequest;
import com.footbook.dto.request.profile.ChangePasswordRequest;
import com.footbook.dto.request.profile.UpdateProfileRequest;
import com.footbook.dto.request.profile.VerifyEmailChangeRequest;
import com.footbook.dto.response.profile.ProfileResponse;
import com.footbook.exception.EmailSendException;
import com.footbook.repository.RoleRepository;
import com.footbook.repository.UserRepository;
import com.footbook.service.ProfileService;
import com.footbook.service.external.cloudinary.ImageUploadService;
import com.footbook.service.external.email.EmailService;
import com.footbook.service.external.email.EmailTemplateService;
import com.footbook.util.Hasher;
import com.footbook.util.TokenGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.footbook.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private static final int EMAIL_CHANGE_MAX_ATTEMPTS = 5;
    private static final int EMAIL_CHANGE_LOCK_MINUTES = 60;
    private static final int EMAIL_CHANGE_CODE_TTL_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Hasher hasher;
    private final TokenGenerator tokenGenerator;
    private final EmailProperties emailProperties;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        User user = getCurrentUser();
        return buildProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(@Valid UpdateProfileRequest request) {
        User user = getCurrentUser();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);

        return buildProfileResponse(user);
    }

    @Override
    public void changePassword(@Valid ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!hasher.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CURRENT_PASSWORD);
        }

        String newPasswordHash = hasher.hash(request.newPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }

    @Override
    public void requestEmailChange(@Valid ChangeEmailRequest request) {
        User user = getCurrentUser();

        if (user.getEmail().equals(request.newEmail())) {
            throw new IllegalArgumentException(NEW_EMAIL_SAME);
        }

        if (userRepository.existsByEmail(request.newEmail())) {
            throw new IllegalArgumentException(EMAIL_IN_USE);
        }

        if (user.getPendingEmailLockedUntil() != null &&
            user.getPendingEmailLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(EMAIL_CHANGE_LOCKED);
        }

        int code = tokenGenerator.generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EMAIL_CHANGE_CODE_TTL_MINUTES);

        user.setPendingEmail(request.newEmail());
        user.setPendingEmailCode(code);
        user.setPendingEmailExpiresAt(expiresAt);
        user.setPendingEmailAttempts(0);
        user.setPendingEmailLockedUntil(null);
        userRepository.save(user);

        try {
            String subject = "Footbook - Email Change";
            String body = emailTemplateService.render("verification", Map.of(
                "code", String.valueOf(code),
                "year", String.valueOf(LocalDateTime.now().getYear()),
                "expiry", String.valueOf(EMAIL_CHANGE_CODE_TTL_MINUTES)
            ));
            emailService.sendEmail(emailProperties.getFrom(), request.newEmail(), subject, body, true);
        } catch (RuntimeException ex) {
            log.error("Failed to send email change verification", ex);
            throw new EmailSendException(FAILED_TO_SEND_EMAIL, ex);
        }
    }

    @Override
    public void verifyEmailChange(@Valid VerifyEmailChangeRequest request) {
        User user = getCurrentUser();

        if (user.getPendingEmail() == null) {
            throw new IllegalStateException(EMAIL_CHANGE_MISMATCH);
        }

        if (user.getPendingEmailLockedUntil() != null &&
            user.getPendingEmailLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(EMAIL_CHANGE_LOCKED);
        }

        if (user.getPendingEmailExpiresAt() == null ||
            user.getPendingEmailExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(EMAIL_CHANGE_EXPIRED);
        }

        if (!request.code().equals(user.getPendingEmailCode())) {
            user.setPendingEmailAttempts(user.getPendingEmailAttempts() + 1);
            if (user.getPendingEmailAttempts() >= EMAIL_CHANGE_MAX_ATTEMPTS) {
                user.setPendingEmailLockedUntil(LocalDateTime.now().plusMinutes(EMAIL_CHANGE_LOCK_MINUTES));
            }
            userRepository.save(user);
            throw new IllegalArgumentException(EMAIL_CHANGE_INVALID);
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setPendingEmailCode(null);
        user.setPendingEmailExpiresAt(null);
        user.setPendingEmailAttempts(0);
        user.setPendingEmailLockedUntil(null);
        userRepository.save(user);
    }

    @Override
    public String uploadProfilePicture(MultipartFile file) {
        User user = getCurrentUser();

        if (user.getProfilePictureUrl() != null) {
            imageUploadService.deleteImage(user.getProfilePictureUrl());
        }

        String imageUrl = imageUploadService.uploadImage(file, "profile-pictures");
        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    @Override
    public void deleteProfilePicture() {
        User user = getCurrentUser();

        if (user.getProfilePictureUrl() != null) {
            imageUploadService.deleteImage(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
            userRepository.save(user);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException(NOT_AUTHENTICATED);
        }
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    private ProfileResponse buildProfileResponse(User user) {
        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        return new ProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getDateOfBirth(),
            user.getProfilePictureUrl(),
            user.getIsVerified(),
            role.getTitle(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
