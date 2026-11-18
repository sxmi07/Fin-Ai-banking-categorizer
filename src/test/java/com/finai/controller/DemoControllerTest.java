package com.finai.controller;

import com.finai.config.DemoDataLoader;
import com.finai.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DemoControllerTest {

    @Test
    @DisplayName("Demo reset endpoint should reload 30 records and return 200 OK")
    void testResetDemoData() {
        // Lightweight subclass stub without heavy Mockito ByteBuddy agent overhead
        DemoDataLoader stubLoader = new DemoDataLoader(null) {
            @Override
            public int reloadDemoData() {
                return 30;
            }
        };

        DemoController controller = new DemoController(stubLoader);
        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.resetDemoData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(30, response.getBody().getData().get("loadedCount"));
    }
}
