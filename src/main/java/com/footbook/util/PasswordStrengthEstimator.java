package com.footbook.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PasswordStrengthEstimator {
    private static final int MIN_LENGTH = 8;
    private static final int GOOD_LENGTH = 12;
    private static final int EXCELLENT_LENGTH = 16;

    private static final String LEVEL_VERY_WEAK = "VERY_WEAK";
    private static final String LEVEL_WEAK = "WEAK";
    private static final String LEVEL_FAIR = "FAIR";
    private static final String LEVEL_GOOD = "GOOD";
    private static final String LEVEL_STRONG = "STRONG";

    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "12345678", "qwerty", "abc123", "monkey",
        "1234567", "letmein", "trustno1", "dragon", "baseball", "iloveyou",
        "master", "sunshine", "ashley", "bailey", "shadow", "123123",
        "654321", "superman", "qazwsx", "michael", "football", "welcome"
    };

    public PasswordStrengthResult estimateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return createWeakResult();
        }

        int score = calculateScore(password);
        List<String> suggestions = generateSuggestions(password);

        String level = determineLevel(score);
        String message = createMessage(level);
        double crackTimeSeconds = estimateCrackTime(password);

        return new PasswordStrengthResult(
            score,
            level,
            message,
            suggestions.toArray(String[]::new),
            crackTimeSeconds
        );
    }

    private PasswordStrengthResult createWeakResult() {
        return new PasswordStrengthResult(
            0,
            LEVEL_VERY_WEAK,
            "Password is required",
            new String[]{"Enter a password"},
            0.0
        );
    }

    private int calculateScore(String password) {
        int score = 0;
        int length = password.length();

        score += calculateLengthScore(length);

        score += calculateVarietyScore(password);

        score = applyPatternAdjustments(password, score);

        return Math.clamp(score, 0, 100);
    }

    private int calculateLengthScore(int length) {
        if (length < MIN_LENGTH) {
            return 0;
        } else if (length >= EXCELLENT_LENGTH) {
            return 40;
        } else if (length >= GOOD_LENGTH) {
            return 30;
        } else {
            return 20;
        }
    }

    private int calculateVarietyScore(String password) {
        int score = 0;

        if (LOWERCASE.matcher(password).find()) score += 10;
        if (UPPERCASE.matcher(password).find()) score += 10;
        if (DIGIT.matcher(password).find()) score += 10;
        if (SPECIAL_CHAR.matcher(password).find()) score += 10;

        return score;
    }

    private int applyPatternAdjustments(String password, int currentScore) {
        int adjustedScore = currentScore;
        String lowerPassword = password.toLowerCase();

        if (containsCommonPassword(lowerPassword)) {
            return Math.max(0, adjustedScore - 30);
        }

        if (hasSequentialChars(password)) {
            adjustedScore = Math.max(0, adjustedScore - 10);
        }
        if (hasRepeatedChars(password)) {
            adjustedScore = Math.max(0, adjustedScore - 10);
        }

        int varietyCount = countVariety(password);
        if (varietyCount >= 3 && password.length() >= GOOD_LENGTH) {
            adjustedScore += 10;
        }
        if (varietyCount == 4 && password.length() >= EXCELLENT_LENGTH) {
            adjustedScore += 10;
        }

        return adjustedScore;
    }

    private boolean containsCommonPassword(String lowerPassword) {
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    private int countVariety(String password) {
        int count = 0;
        if (LOWERCASE.matcher(password).find()) count++;
        if (UPPERCASE.matcher(password).find()) count++;
        if (DIGIT.matcher(password).find()) count++;
        if (SPECIAL_CHAR.matcher(password).find()) count++;
        return count;
    }

    private List<String> generateSuggestions(String password) {
        List<String> suggestions = new ArrayList<>();
        int length = password.length();

        if (length < MIN_LENGTH) {
            suggestions.add("Use at least " + MIN_LENGTH + " characters");
        } else if (length < GOOD_LENGTH) {
            suggestions.add("Use " + GOOD_LENGTH + "+ characters for better security");
        }

        if (!LOWERCASE.matcher(password).find()) {
            suggestions.add("Add lowercase letters (a-z)");
        }
        if (!UPPERCASE.matcher(password).find()) {
            suggestions.add("Add uppercase letters (A-Z)");
        }
        if (!DIGIT.matcher(password).find()) {
            suggestions.add("Add numbers (0-9)");
        }
        if (!SPECIAL_CHAR.matcher(password).find()) {
            suggestions.add("Add special characters (!@#$%...)");
        }

        if (containsCommonPassword(password.toLowerCase())) {
            suggestions.add("Avoid common passwords");
        } else {
            if (hasSequentialChars(password)) {
                suggestions.add("Avoid sequential characters (123, abc)");
            }
            if (hasRepeatedChars(password)) {
                suggestions.add("Avoid repeated characters (aaa, 111)");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Password looks good!");
        }

        return suggestions;
    }

    private String determineLevel(int score) {
        if (score < 20) {
            return LEVEL_VERY_WEAK;
        } else if (score < 40) {
            return LEVEL_WEAK;
        } else if (score < 60) {
            return LEVEL_FAIR;
        } else if (score < 80) {
            return LEVEL_GOOD;
        } else {
            return LEVEL_STRONG;
        }
    }

    private String createMessage(String level) {
        return switch (level) {
            case LEVEL_VERY_WEAK -> "Very weak password";
            case LEVEL_WEAK -> "Weak password";
            case LEVEL_FAIR -> "Fair password";
            case LEVEL_GOOD -> "Good password";
            case LEVEL_STRONG -> "Strong password";
            default -> "Unknown strength";
        };
    }

    private boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();
        for (int i = 0; i < lower.length() - 2; i++) {
            char c1 = lower.charAt(i);
            char c2 = lower.charAt(i + 1);
            char c3 = lower.charAt(i + 2);

            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRepeatedChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    private double estimateCrackTime(String password) {
        int charsetSize = 0;
        if (LOWERCASE.matcher(password).find()) charsetSize += 26;
        if (UPPERCASE.matcher(password).find()) charsetSize += 26;
        if (DIGIT.matcher(password).find()) charsetSize += 10;
        if (SPECIAL_CHAR.matcher(password).find()) charsetSize += 32;

        if (charsetSize == 0) charsetSize = 26;

        double combinations = Math.pow(charsetSize, password.length());
        double attemptsPerSecond = 1_000_000_000.0;

        return combinations / attemptsPerSecond / 2;
    }

    public record PasswordStrengthResult(
        int score,
        String level,
        String message,
        String[] suggestions,
        double crackTimeSeconds
    ) {
        public String getFormattedCrackTime() {
            if (crackTimeSeconds < 1) {
                return "Instant";
            } else if (crackTimeSeconds < 60) {
                return String.format("%.0f seconds", crackTimeSeconds);
            } else if (crackTimeSeconds < 3600) {
                return String.format("%.0f minutes", crackTimeSeconds / 60);
            } else if (crackTimeSeconds < 86400) {
                return String.format("%.0f hours", crackTimeSeconds / 3600);
            } else if (crackTimeSeconds < 2592000) {
                return String.format("%.0f days", crackTimeSeconds / 86400);
            } else if (crackTimeSeconds < 31536000) {
                return String.format("%.0f months", crackTimeSeconds / 2592000);
            } else {
                return String.format("%.0f years", crackTimeSeconds / 31536000);
            }
        }
    }
}
