package com.finai.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    @Test
    @DisplayName("Health endpoint should return UP status and telemetry")
    void testGetHealthStatus() {
        HealthController controller = new HealthController();
        ReflectionTestUtils.setField(controller, "applicationName", "FinAI");
        ReflectionTestUtils.setField(controller, "aiProvider", "gemini");
        ReflectionTestUtils.setField(controller, "geminiApiKey", "");

        ResponseEntity<Map<String, Object>> response = controller.getHealthStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("FinAI", response.getBody().get("application"));
        assertEquals("DEMO_FALLBACK_MODE", response.getBody().get("mode"));
        assertFalse((Boolean) response.getBody().get("aiKeyConfigured"));

        Map<?, ?> mem = (Map<?, ?>) response.getBody().get("memoryStatus");
        assertNotNull(mem);
        assertTrue(mem.containsKey("jvmMaxMb"));
    }

    @Test
    @DisplayName("Health endpoint should report AI_ENABLED when API key is set")
    void testGetHealthStatus_WithKey() {
        HealthController controller = new HealthController();
        ReflectionTestUtils.setField(controller, "applicationName", "FinAI");
        ReflectionTestUtils.setField(controller, "aiProvider", "gemini");
        ReflectionTestUtils.setField(controller, "geminiApiKey", "ai_test_key_123");

        ResponseEntity<Map<String, Object>> response = controller.getHealthStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AI_ENABLED", response.getBody().get("mode"));
        assertTrue((Boolean) response.getBody().get("aiKeyConfigured"));
    }
}
