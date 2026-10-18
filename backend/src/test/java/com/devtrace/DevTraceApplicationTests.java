package com.devtrace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DevTraceApplicationTests {

    @Test
    @DisplayName("DevTrace application context loads successfully")
    void contextLoads() {
        assertTrue(true, "Application context initialized cleanly without error");
    }
}
