package com.finai.controller;

import com.finai.config.DemoDataLoader;
import com.finai.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for demo data reset operations.
 * Implements POST /api/demo/reset as requested.
 */
@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    private final DemoDataLoader demoDataLoader;

    public DemoController(DemoDataLoader demoDataLoader) {
        this.demoDataLoader = demoDataLoader;
    }

    /**
     * POST /api/demo/reset
     * Reset H2 database back to the 30 fictional sample transactions.
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetDemoData() {
        int loaded = demoDataLoader.reloadDemoData();
        return ResponseEntity.ok(ApiResponse.success(
                "Demonstration data reset successfully to " + loaded + " sample transactions.",
                Map.of("loadedCount", loaded, "status", "RESET_COMPLETE")
        ));
    }
}
