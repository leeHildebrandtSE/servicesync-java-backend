package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.model.dto.EmployeeDto;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve employee information by ID")
    public ResponseEntity<EmployeeDto> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable UUID id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/employee-id/{employeeId}")
    @Operation(summary = "Get employee by employee ID", description = "Retrieve employee information by employee ID")
    public ResponseEntity<EmployeeDto> getEmployeeByEmployeeId(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        EmployeeDto employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get employees by hospital", description = "Retrieve all employees for specific hospital")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByHospital(
            @Parameter(description = "Hospital ID") @PathVariable UUID hospitalId) {
        List<EmployeeDto> employees = employeeService.getEmployeesByHospital(hospitalId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get employees by role", description = "Retrieve all employees with specific role")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByRole(
            @Parameter(description = "Employee role") @PathVariable EmployeeRole role) {
        List<EmployeeDto> employees = employeeService.getEmployeesByRole(role);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update employee information")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable UUID id,
            @RequestBody EmployeeDto employeeDto) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(updatedEmployee);
    }
}