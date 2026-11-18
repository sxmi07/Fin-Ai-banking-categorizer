package com.finai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check controller providing system status and environment readiness.
 * Endpoint: GET /api/health
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name:FinAI}")
    private String applicationName;

    @Value("${finai.ai.provider:gemini}")
    private String aiProvider;

    @Value("${finai.ai.gemini.api-key:}")
    private String geminiApiKey;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("version", "1.0.0-SNAPSHOT");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("aiProvider", aiProvider);
        health.put("aiKeyConfigured", geminiApiKey != null && !geminiApiKey.isBlank());
        health.put("mode", (geminiApiKey != null && !geminiApiKey.isBlank()) ? "AI_ENABLED" : "DEMO_FALLBACK_MODE");
        health.put("memoryStatus", getMemorySnapshot());
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> getMemorySnapshot() {
        Runtime runtime = Runtime.getRuntime();
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("jvmMaxMb", maxMem);
        mem.put("jvmUsedMb", usedMem);
        mem.put("jvmFreeMb", freeMem);
        return mem;
    }
}
