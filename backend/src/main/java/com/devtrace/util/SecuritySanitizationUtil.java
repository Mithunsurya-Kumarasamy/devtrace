package com.devtrace.util;

import java.util.regex.Pattern;

public final class SecuritySanitizationUtil {

    private static final Pattern SAFE_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");

    private SecuritySanitizationUtil() {}

    /**
     * Masks authorization bearer tokens or secret keys for safe diagnostic logging.
     */
    public static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "[UNSET]";
        }
        int len = token.length();
        if (len <= 8) {
            return "***";
        }
        String prefix = token.substring(0, Math.min(4, len / 4));
        String suffix = token.substring(len - Math.min(4, len / 4));
        return prefix + "..." + suffix + " (len=" + len + ")";
    }

    /**
     * Sanitizes input to prevent CRLF log injection.
     */
    public static String sanitizeForLogs(String input) {
        if (input == null) return "null";
        return input.replace('\r', '_').replace('\n', '_');
    }

    /**
     * Validates that a string does not contain path traversal or unsafe characters.
     */
    public static boolean isSafeIdentifier(String identifier) {
        return identifier != null && SAFE_IDENTIFIER_PATTERN.matcher(identifier).matches();
    }
}
