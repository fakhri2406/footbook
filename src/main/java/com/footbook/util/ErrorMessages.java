package com.footbook.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessages {
    // Common
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String EMAIL_IN_USE = "Email already in use";
    public static final String INVALID_REQUEST = "Invalid request";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String CANNOT_DELETE_SELF = "Cannot delete your own account";
    public static final String LAST_ADMIN_GUARD = "Cannot remove or demote the last admin";

    // Auth
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String ACCOUNT_NOT_VERIFIED = "Account not verified";
    public static final String ACCOUNT_LOCKED = "Account locked. Try later";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
    public static final String REFRESH_TOKEN_MISMATCH = "Refresh token mismatch";
    public static final String INVALID_ACCESS_TOKEN = "Invalid access token";
    public static final String ACCESS_TOKEN_REVOKED = "Access token has been revoked";

    // Verification
    public static final String VERIFICATION_LOCKED = "Verification locked. Try later";
    public static final String VERIFICATION_EXPIRED = "Verification code expired";
    public static final String VERIFICATION_INVALID = "Invalid verification code";
    public static final String FAILED_TO_SEND_EMAIL = "Failed to send email";
    public static final String FAILED_TO_LOAD_TEMPLATE = "Failed to load email template";
    public static final String RESEND_COOLDOWN = "Please wait before requesting a new code";

    // Password or Email change
    public static final String INVALID_CURRENT_PASSWORD = "Invalid current password";
    public static final String PASSWORD_POLICY = "Password must be 8+ chars, include uppercase and digit";
    public static final String NEW_EMAIL_SAME = "New email must be different";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String EMAIL_CHANGE_LOCKED = "Email change locked. Try later";
    public static final String EMAIL_CHANGE_MISMATCH = "Pending email mismatch";
    public static final String EMAIL_CHANGE_EXPIRED = "Email change code expired";
    public static final String EMAIL_CHANGE_INVALID = "Invalid email change code";
    public static final String PASSWORD_RESET_LOCKED = "Password reset locked. Try later";
    public static final String PASSWORD_RESET_EXPIRED = "Password reset token expired";
    public static final String PASSWORD_RESET_INVALID = "Invalid password reset token";

    // Image Upload
    public static final String INVALID_FILE_TYPE = "Invalid file type. Only images are allowed";
    public static final String FILE_TOO_LARGE = "File size exceeds the maximum allowed limit";
    public static final String UPLOAD_FAILED = "File upload failed";

    // Branch
    public static final String BRANCH_NOT_FOUND = "Branch not found";
    public static final String BRANCH_INACTIVE = "Branch not found or inactive";
    public static final String OPERATING_HOURS_INVALID = "Operating hours end must be after operating hours start";
    public static final String TIME_FORMAT_INVALID = "must be in HH:mm format (e.g., 09:00)";
    public static final String DATE_FORMAT_INVALID = "Date must be in yyyy-MM-dd format";
    public static final String OUTSIDE_OPERATING_HOURS = "Booking time must be within branch operating hours";

    // Individual Room
    public static final String ROOM_NOT_FOUND = "Room not found";
    public static final String ROOM_CANCELLED = "Room not found or cancelled";
    public static final String ROOM_FULL = "Room is already full";
    public static final String ALREADY_JOINED = "You have already joined this room";
    public static final String TIME_CONFLICT = "You have a conflicting booking at this time";
    public static final String BOOKING_IN_PAST = "Cannot create a room in the past";
    public static final String END_TIME_BEFORE_START = "End time must be after start time";
    public static final String OWNER_CANNOT_LEAVE = "Room owner cannot leave the room. Please cancel the room instead.";
    public static final String NOT_PARTICIPANT = "You are not a participant in this room";
    public static final String NOT_OWNER = "Only the room owner can cancel the room";

    // Team
    public static final String TEAM_NOT_FOUND = "Team not found";
    public static final String TEAM_DISBANDED = "Team not found or disbanded";
    public static final String NOT_CAPTAIN = "Only the team captain can perform this action";
    public static final String ALREADY_MEMBER = "User is already a member of this team";
    public static final String TEAM_FULL = "Team is already at full capacity";
    public static final String CANNOT_REMOVE_CAPTAIN = "Cannot remove the team captain. Transfer captain role first or disband the team.";
    public static final String NOT_TEAM_MEMBER = "User is not a member of this team";
    public static final String ALREADY_CAPTAIN = "You are already the captain";
    public static final String NEW_CAPTAIN_NOT_MEMBER = "New captain must be a member of the team";
    public static final String TEAM_NOT_FULL_ROSTER = "Team must have full roster to perform this action";

    // Team Room
    public static final String TEAM_ROOM_MATCHED = "Room is already matched";
    public static final String CANNOT_JOIN_OWN_ROOM = "Cannot join your own team's room";
    public static final String TEAM_SIZE_MISMATCH = "Team size mismatch";
    public static final String TEAM_CONFLICT = "Your team has a conflicting team room booking at this time";
    public static final String TEAM_MEMBERS_CONFLICT = "One or more team members have conflicting individual room bookings at this time";
    public static final String ONLY_CREATOR_CAPTAIN_CAN_CANCEL = "Only the creator team's captain can cancel the room";

    // Notification
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found";
    public static final String NOT_YOUR_NOTIFICATION = "You can only access your own notifications";
    public static final String INVALID_NOTIFICATION_TYPE = "Invalid notification type";
}
