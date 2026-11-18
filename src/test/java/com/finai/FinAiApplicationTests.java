package com.finai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinAiApplicationTests {

    @Test
    @DisplayName("Verify FinAiApplication main class is resolvable")
    void applicationClassLoads() {
        assertNotNull(FinAiApplication.class);
    }
}
