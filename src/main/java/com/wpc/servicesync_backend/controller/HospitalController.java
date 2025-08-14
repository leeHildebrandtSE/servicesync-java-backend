// src/main/java/com/wpc/servicesync_backend/controller/HospitalController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.dto.HospitalResponse;
import com.wpc.servicesync_backend.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hospitals", description = "Hospital management endpoints")
@CrossOrigin(origins = "*")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @Operation(summary = "Get all active hospitals", description = "Retrieve all active hospitals")
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> getAllActiveHospitals() {
        log.info("Fetching all active hospitals");

        try {
            List<HospitalResponse> hospitals = hospitalService.getAllActiveHospitals();
            return ResponseEntity.ok(ApiResponse.success(hospitals));
        } catch (Exception e) {
            log.error("Error fetching hospitals", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch hospitals", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hospital by ID", description = "Retrieve hospital details by ID")
    public ResponseEntity<ApiResponse<HospitalResponse>> getHospitalById(
            @Parameter(description = "Hospital ID") @PathVariable UUID id) {
        log.info("Fetching hospital with ID: {}", id);

        try {
            HospitalResponse hospital = hospitalService.getHospitalById(id);
            return ResponseEntity.ok(ApiResponse.success(hospital));
        } catch (RuntimeException e) {
            log.error("Hospital not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching hospital", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch hospital", e.getMessage()));
        }
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get hospital statistics", description = "Get performance statistics for a specific hospital")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getHospitalStats(
            @Parameter(description = "Hospital ID") @PathVariable UUID id) {
        log.info("Fetching statistics for hospital: {}", id);

        // Placeholder for hospital statistics
        Object stats = hospitalService.getHospitalStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}