// src/main/java/com/wpc/servicesync_backend/controller/ServiceSessionController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.dto.QRScanRequest;
import com.wpc.servicesync_backend.dto.ServiceSessionRequest;
import com.wpc.servicesync_backend.dto.ServiceSessionResponse;
import com.wpc.servicesync_backend.model.dto.SessionUpdateRequest;
import com.wpc.servicesync_backend.service.ServiceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Sessions", description = "Meal delivery session management")
@CrossOrigin(origins = "*")
public class ServiceSessionController {

    private final ServiceSessionService sessionService;

    @PostMapping
    @Operation(summary = "Create service session", description = "Create a new meal delivery session")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> createSession(
            @Valid @RequestBody ServiceSessionRequest request) {
        log.info("Creating new service session for employee: {} and ward: {}",
                request.getEmployeeId(), request.getWardId());

        try {
            ServiceSessionResponse session = sessionService.createSession(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Session created successfully", session));
        } catch (Exception e) {
            log.error("Error creating session", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create session", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/qr-scan")
    @Operation(summary = "Process QR scan", description = "Process QR code scan for session workflow")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> scanQR(
            @PathVariable UUID sessionId,
            @Valid @RequestBody QRScanRequest request) {
        log.info("Processing QR scan for session: {}", sessionId);

        try {
            request.setSessionId(sessionId);
            ServiceSessionResponse session = sessionService.scanQR(request);
            return ResponseEntity.ok(
                    ApiResponse.success("QR scan processed successfully", session));
        } catch (Exception e) {
            log.error("Error processing QR scan", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process QR scan", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/alert-nurse")
    @Operation(summary = "Alert nurse", description = "Send alert to nurse for meal delivery")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> alertNurse(@PathVariable UUID sessionId) {
        log.info("Alerting nurse for session: {}", sessionId);

        try {
            ServiceSessionResponse session = sessionService.alertNurse(sessionId);
            return ResponseEntity.ok(
                    ApiResponse.success("Nurse alert sent successfully", session));
        } catch (Exception e) {
            log.error("Error sending nurse alert", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send nurse alert", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/nurse-response")
    @Operation(summary = "Record nurse response", description = "Record nurse acknowledgment of meal delivery")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> nurseResponse(
            @PathVariable UUID sessionId,
            @RequestParam String nurseName) {
        log.info("Recording nurse response for session: {} by nurse: {}", sessionId, nurseName);

        try {
            ServiceSessionResponse session = sessionService.nurseResponse(sessionId, nurseName);
            return ResponseEntity.ok(
                    ApiResponse.success("Nurse response recorded successfully", session));
        } catch (Exception e) {
            log.error("Error recording nurse response", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to record nurse response", e.getMessage()));
        }
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Update session details and progress")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> updateSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SessionUpdateRequest request) {
        log.info("Updating session: {}", sessionId);

        try {
            request.setSessionId(sessionId);
            ServiceSessionResponse session = sessionService.updateSession(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Session updated successfully", session));
        } catch (Exception e) {
            log.error("Error updating session", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update session", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete session", description = "Mark session as completed")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> completeSession(@PathVariable UUID sessionId) {
        log.info("Completing session: {}", sessionId);

        try {
            ServiceSessionResponse session = sessionService.completeSession(sessionId);
            return ResponseEntity.ok(
                    ApiResponse.success("Session completed successfully", session));
        } catch (Exception e) {
            log.error("Error completing session", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to complete session", e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session by ID", description = "Retrieve session details by session ID")
    public ResponseEntity<ApiResponse<ServiceSessionResponse>> getSession(@PathVariable String sessionId) {
        log.info("Fetching session: {}", sessionId);

        return sessionService.findBySessionId(sessionId)
                .map(session -> ResponseEntity.ok(ApiResponse.success(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}/active")
    @Operation(summary = "Get active sessions by employee", description = "Get all active sessions for an employee")
    public ResponseEntity<ApiResponse<List<ServiceSessionResponse>>> getActiveSessionsByEmployee(
            @PathVariable UUID employeeId) {
        log.info("Fetching active sessions for employee: {}", employeeId);

        List<ServiceSessionResponse> sessions = sessionService.findActiveSessionsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/employee/{employeeId}/recent")
    @Operation(summary = "Get recent sessions by employee", description = "Get recent sessions for an employee")
    public ResponseEntity<ApiResponse<List<ServiceSessionResponse>>> getRecentSessionsByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "24") int hours) {
        log.info("Fetching recent sessions for employee: {} within {} hours", employeeId, hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<ServiceSessionResponse> sessions = sessionService.findRecentSessionsByEmployee(employeeId, since);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed sessions", description = "Get paginated list of completed sessions")
    public ResponseEntity<ApiResponse<Page<ServiceSessionResponse>>> getCompletedSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("Fetching completed sessions - page: {}, size: {}", page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ServiceSessionResponse> sessions = sessionService.findCompletedSessions(pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active sessions", description = "Get all currently active sessions")
    public ResponseEntity<ApiResponse<List<ServiceSessionResponse>>> getAllActiveSessions() {
        log.info("Fetching all active sessions");

        List<ServiceSessionResponse> sessions = sessionService.findAllActiveSessions();
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }
}