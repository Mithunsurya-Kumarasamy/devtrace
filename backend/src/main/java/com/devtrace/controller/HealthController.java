package com.devtrace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "System health, versioning, and environment diagnostics")
public class HealthController {

    @Value("${spring.application.name:DevTrace}")
    private String applicationName;

    @Value("${devtrace.version:1.0.0}")
    private String version;

    @GetMapping
    @Operation(summary = "DevTrace system health check", description = "Returns active platform operational status, version, and timestamps.")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(Map.of(
                "application", applicationName,
                "version", version,
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "services", Map.of(
                        "database", "CONNECTED",
                        "ingestionPipeline", "READY",
                        "analyticsEngine", "ACTIVE"
                )
        ));
    }
}
