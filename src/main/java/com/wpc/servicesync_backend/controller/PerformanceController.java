package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.model.dto.PerformanceReportDto;
import com.wpc.servicesync_backend.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "Performance", description = "Performance analytics and reporting endpoints")
@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/daily")
    @Operation(summary = "Generate daily performance report", description = "Generate performance report for specific day")
    public ResponseEntity<PerformanceReportDto> getDailyReport(
            @Parameter(description = "Report date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        PerformanceReportDto report = performanceService.generateDailyReport(date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/weekly")
    @Operation(summary = "Generate weekly performance report", description = "Generate performance report for specific week")
    public ResponseEntity<PerformanceReportDto> getWeeklyReport(
            @Parameter(description = "Week start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime weekStart) {
        PerformanceReportDto report = performanceService.generateWeeklyReport(weekStart);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Generate monthly performance report", description = "Generate performance report for specific month")
    public ResponseEntity<PerformanceReportDto> getMonthlyReport(
            @Parameter(description = "Month start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime monthStart) {
        PerformanceReportDto report = performanceService.generateMonthlyReport(monthStart);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Generate hospital performance report", description = "Generate performance report for specific hospital")
    public ResponseEntity<PerformanceReportDto> getHospitalReport(
            @Parameter(description = "Hospital ID") @PathVariable UUID hospitalId,
            @Parameter(description = "From date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate) {
        PerformanceReportDto report = performanceService.generateHospitalReport(hospitalId, fromDate);
        return ResponseEntity.ok(report);
    }
}