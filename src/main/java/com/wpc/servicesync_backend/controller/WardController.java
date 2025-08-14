// src/main/java/com/wpc/servicesync_backend/controller/WardController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.dto.WardResponse;
import com.wpc.servicesync_backend.service.WardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wards", description = "Ward management endpoints")
@CrossOrigin(origins = "*")
public class WardController {

    private final WardService wardService;

    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get wards by hospital", description = "Retrieve all active wards for a specific hospital")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getWardsByHospital(
            @Parameter(description = "Hospital ID") @PathVariable UUID hospitalId) {
        log.info("Fetching wards for hospital: {}", hospitalId);

        try {
            List<WardResponse> wards = wardService.getWardsByHospital(hospitalId);
            return ResponseEntity.ok(ApiResponse.success(wards));
        } catch (Exception e) {
            log.error("Error fetching wards for hospital: {}", hospitalId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch wards", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ward by ID", description = "Retrieve ward details by ID")
    public ResponseEntity<ApiResponse<WardResponse>> getWardById(
            @Parameter(description = "Ward ID") @PathVariable UUID id) {
        log.info("Fetching ward with ID: {}", id);

        try {
            WardResponse ward = wardService.getWardById(id);
            return ResponseEntity.ok(ApiResponse.success(ward));
        } catch (RuntimeException e) {
            log.error("Ward not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching ward", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch ward", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all active wards", description = "Retrieve all active wards across all hospitals")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getAllActiveWards() {
        log.info("Fetching all active wards");

        try {
            List<WardResponse> wards = wardService.getAllActiveWards();
            return ResponseEntity.ok(ApiResponse.success(wards));
        } catch (Exception e) {
            log.error("Error fetching all wards", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch wards", e.getMessage()));
        }
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Get ward sessions", description = "Get current sessions for a specific ward")
    public ResponseEntity<ApiResponse<Object>> getWardSessions(
            @Parameter(description = "Ward ID") @PathVariable UUID id) {
        log.info("Fetching sessions for ward: {}", id);

        try {
            Object sessions = wardService.getWardSessions(id);
            return ResponseEntity.ok(ApiResponse.success(sessions));
        } catch (Exception e) {
            log.error("Error fetching ward sessions", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch ward sessions", e.getMessage()));
        }
    }
}