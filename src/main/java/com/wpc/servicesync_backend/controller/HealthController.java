package com.wpc.servicesync_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health Check", description = "Application health monitoring")
@CrossOrigin(origins = "*")
public class HealthController implements HealthIndicator {

    private final Environment environment;
    private final DataSource dataSource;

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the application is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {  // Renamed method
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            // Basic health info
            healthInfo.put("status", "UP");
            healthInfo.put("timestamp", LocalDateTime.now());
            healthInfo.put("service", "ServiceSync API");
            healthInfo.put("version", getVersion());
            healthInfo.put("environment", getCurrentProfile());

            // Database connectivity check
            healthInfo.put("database", checkDatabaseHealth());

            // System info
            healthInfo.put("system", getSystemInfo());

            return ResponseEntity.ok(healthInfo);
        } catch (Exception e) {
            log.error("Health check failed", e);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(503).body(healthInfo);
        }
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint", description = "Simple ping endpoint for monitoring")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Check if application is ready to serve traffic")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();

        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseConnectivity();

            readiness.put("ready", dbHealthy);
            readiness.put("timestamp", LocalDateTime.now());
            readiness.put("checks", Map.of(
                    "database", dbHealthy ? "UP" : "DOWN"
            ));

            if (dbHealthy) {
                return ResponseEntity.ok(readiness);
            } else {
                return ResponseEntity.status(503).body(readiness);
            }
        } catch (Exception e) {
            log.error("Readiness check failed", e);
            readiness.put("ready", false);
            readiness.put("error", e.getMessage());
            return ResponseEntity.status(503).body(readiness);
        }
    }

    @GetMapping("/live")
    @Operation(summary = "Liveness check", description = "Check if application is alive")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = Map.of(
                "alive", true,
                "timestamp", LocalDateTime.now(),
                "uptime", getUptime()
        );
        return ResponseEntity.ok(liveness);
    }

    // This method implements HealthIndicator interface
    @Override
    public Health health() {
        try {
            boolean dbHealthy = checkDatabaseConnectivity();
            if (dbHealthy) {
                return Health.up()
                        .withDetail("database", "UP")
                        .withDetail("version", getVersion())
                        .withDetail("timestamp", LocalDateTime.now())
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "DOWN")
                        .withDetail("timestamp", LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        }
    }

    private boolean checkDatabaseConnectivity() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            log.warn("Database connectivity check failed", e);
            return false;
        }
    }

    private Map<String, Object> checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            return Map.of(
                    "status", isValid ? "UP" : "DOWN",
                    "url", connection.getMetaData().getURL(),
                    "driver", connection.getMetaData().getDriverName(),
                    "version", connection.getMetaData().getDatabaseProductVersion()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            );
        }
    }

    private String getVersion() {
        try {
            // Try to get version from build properties if available
            return environment.getProperty("app.version", "1.0.0");
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private String getCurrentProfile() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? String.join(",", profiles) : "default";
    }

    private Map<String, Object> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "processors", runtime.availableProcessors(),
                "maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB",
                "totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB",
                "freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB",
                "javaVersion", System.getProperty("java.version"),
                "timezone", System.getProperty("user.timezone")
        );
    }

    private String getUptime() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}