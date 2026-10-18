package com.devtrace.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecuritySanitizationUtilTest {

    @Test
    void testMaskToken() {
        assertThat(SecuritySanitizationUtil.maskToken(null)).isEqualTo("[UNSET]");
        assertThat(SecuritySanitizationUtil.maskToken("")).isEqualTo("[UNSET]");
        assertThat(SecuritySanitizationUtil.maskToken("abc")).isEqualTo("***");
        assertThat(SecuritySanitizationUtil.maskToken("12345678")).isEqualTo("***");

        String token = "ghp_1234567890abcdefghijklmnopqrstuvwxyz";
        String masked = SecuritySanitizationUtil.maskToken(token);
        assertThat(masked).startsWith("ghp_");
        assertThat(masked).endsWith("wxyz (len=40)");
        assertThat(masked).doesNotContain("1234567890abcdefghijklmnopqr");
    }

    @Test
    void testSanitizeForLogs() {
        assertThat(SecuritySanitizationUtil.sanitizeForLogs(null)).isEqualTo("null");
        assertThat(SecuritySanitizationUtil.sanitizeForLogs("safe_input")).isEqualTo("safe_input");
        assertThat(SecuritySanitizationUtil.sanitizeForLogs("line1\r\nline2")).isEqualTo("line1__line2");
    }

    @Test
    void testIsSafeIdentifier() {
        assertThat(SecuritySanitizationUtil.isSafeIdentifier("devtrace-org")).isTrue();
        assertThat(SecuritySanitizationUtil.isSafeIdentifier("repo.name_123")).isTrue();
        assertThat(SecuritySanitizationUtil.isSafeIdentifier("repo/name")).isFalse();
        assertThat(SecuritySanitizationUtil.isSafeIdentifier("repo;rm -rf /")).isFalse();
        assertThat(SecuritySanitizationUtil.isSafeIdentifier(null)).isFalse();
    }
}
