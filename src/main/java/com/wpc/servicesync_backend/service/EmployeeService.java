// src/main/java/com/wpc/servicesync_backend/service/EmployeeService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.EmployeeLoginRequest;
import com.wpc.servicesync_backend.dto.EmployeeResponse;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<EmployeeResponse> authenticate(EmployeeLoginRequest request) {
        log.info("Authenticating employee with ID: {}", request.getEmployeeId());

        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(request.getEmployeeId());

        if (employeeOpt.isEmpty()) {
            log.warn("Employee not found with ID: {}", request.getEmployeeId());
            return Optional.empty();
        }

        Employee employee = employeeOpt.get();

        if (!employee.getIsActive()) {
            log.warn("Inactive employee attempted login: {}", request.getEmployeeId());
            return Optional.empty();
        }

        // In a real implementation, you'd check the password
        // For demo purposes, we'll accept any password
        // if (!passwordEncoder.matches(request.getPassword(), employee.getPasswordHash())) {
        //     log.warn("Invalid password for employee: {}", request.getEmployeeId());
        //     return Optional.empty();
        // }

        // Update last login
        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);

        log.info("Employee authenticated successfully: {}", request.getEmployeeId());
        return Optional.of(mapToResponse(employee));
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> findByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> findById(UUID id) {
        return employeeRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findByRole(EmployeeRole role) {
        return employeeRepository.findByRoleAndIsActiveTrue(role)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findByHospital(UUID hospitalId) {
        return employeeRepository.findByHospitalAndRole(hospitalId, null)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findActiveEmployees() {
        return employeeRepository.findAllActiveEmployeesOrderByHospitalAndRole()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .hospitalName(employee.getHospital().getName())
                .hospitalCode(employee.getHospital().getCode())
                .shiftSchedule(employee.getShiftSchedule())
                .isActive(employee.getIsActive())
                .lastLogin(employee.getLastLogin())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}