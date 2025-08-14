// src/main/java/com/wpc/servicesync_backend/controller/AuthController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.dto.EmployeeLoginRequest;
import com.wpc.servicesync_backend.dto.EmployeeResponse;
import com.wpc.servicesync_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Employee authentication endpoints")
@CrossOrigin(origins = "*")
public class AuthController {

    private final EmployeeService employeeService;

    @PostMapping("/login")
    @Operation(summary = "Employee login", description = "Authenticate employee with ID and password")
    public ResponseEntity<ApiResponse<EmployeeResponse>> login(@Valid @RequestBody EmployeeLoginRequest request) {
        log.info("Login attempt for employee ID: {}", request.getEmployeeId());

        return employeeService.authenticate(request)
                .map(employee -> ResponseEntity.ok(
                        ApiResponse.success("Login successful", employee)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid credentials")));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee by ID", description = "Retrieve employee information by employee ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable String employeeId) {
        log.info("Fetching employee with ID: {}", employeeId);

        return employeeService.findByEmployeeId(employeeId)
                .map(employee -> ResponseEntity.ok(
                        ApiResponse.success(employee)))
                .orElse(ResponseEntity.notFound().build());
    }
}