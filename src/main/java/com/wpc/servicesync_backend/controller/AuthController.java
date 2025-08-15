// File: src/main/java/com/wpc/servicesync_backend/controller/AuthController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.model.dto.AuthenticationRequest;
import com.wpc.servicesync_backend.model.dto.AuthenticationResponse;
import com.wpc.servicesync_backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Employee authentication endpoints")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Employee login", description = "Authenticate employee with ID and password")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Authentication attempt for employee ID: {}", request.getEmployeeId());

        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Authentication failed for employee: {}", request.getEmployeeId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Parameter(description = "Refresh token") @RequestBody String refreshToken) {
        log.info("Token refresh attempt");

        try {
            AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout employee (client-side token removal)")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("Logout request");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Token should be removed from client"));
    }
}