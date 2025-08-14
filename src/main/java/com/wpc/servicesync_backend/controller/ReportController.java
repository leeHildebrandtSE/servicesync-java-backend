// File: src/main/java/com/wpc/servicesync_backend/controller/ReportController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.model.dto.PerformanceReportDto;
import com.wpc.servicesync_backend.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Performance and analytics reports")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
public class ReportController {

    private final PerformanceService performanceService;

    @GetMapping("/performance/daily")
    @Operation(summary = "Daily performance report", description = "Generate daily performance report")
    public ResponseEntity<ApiResponse<PerformanceReportDto>> getDailyReport(
            @Parameter(description = "Report date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Generating daily performance report for date: {}", date);

        try {
            PerformanceReportDto report = performanceService.generateDailyReport(date);
            return ResponseEntity.ok(ApiResponse.success("Daily report generated successfully", report));
        } catch (Exception e) {
            log.error("Error generating daily report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate daily report", e.getMessage()));
        }
    }

    @GetMapping("/performance/weekly")
    @Operation(summary = "Weekly performance report", description = "Generate weekly performance report")
    public ResponseEntity<ApiResponse<PerformanceReportDto>> getWeeklyReport(
            @Parameter(description = "Week start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart) {
        log.info("Generating weekly performance report for week starting: {}", weekStart);

        try {
            PerformanceReportDto report = performanceService.generateWeeklyReport(weekStart);
            return ResponseEntity.ok(ApiResponse.success("Weekly report generated successfully", report));
        } catch (Exception e) {
            log.error("Error generating weekly report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate weekly report", e.getMessage()));
        }
    }

    @GetMapping("/performance/monthly")
    @Operation(summary = "Monthly performance report", description = "Generate monthly performance report")
    public ResponseEntity<ApiResponse<PerformanceReportDto>> getMonthlyReport(
            @Parameter(description = "Month start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthStart) {
        log.info("Generating monthly performance report for month starting: {}", monthStart);

        try {
            PerformanceReportDto report = performanceService.generateMonthlyReport(monthStart);
            return ResponseEntity.ok(ApiResponse.success("Monthly report generated successfully", report));
        } catch (Exception e) {
            log.error("Error generating monthly report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate monthly report", e.getMessage()));
        }
    }

    @GetMapping("/performance/hospital/{hospitalId}")
    @Operation(summary = "Hospital performance report", description = "Generate performance report for specific hospital")
    public ResponseEntity<ApiResponse<PerformanceReportDto>> getHospitalReport(
            @Parameter(description = "Hospital ID") @PathVariable UUID hospitalId,
            @Parameter(description = "From date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate) {
        log.info("Generating hospital performance report for hospital: {} from date: {}", hospitalId, fromDate);

        try {
            PerformanceReportDto report = performanceService.generateHospitalReport(hospitalId, fromDate);
            return ResponseEntity.ok(ApiResponse.success("Hospital report generated successfully", report));
        } catch (Exception e) {
            log.error("Error generating hospital report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate hospital report", e.getMessage()));
        }
    }
}