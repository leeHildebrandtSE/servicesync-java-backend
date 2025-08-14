package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.EmployeeLoginRequest;
import com.wpc.servicesync_backend.dto.EmployeeResponse;
import com.wpc.servicesync_backend.exception.ServiceException;
import com.wpc.servicesync_backend.model.dto.EmployeeDto;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // For production, uncomment password validation:
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
    @Cacheable(value = "employees", key = "#employeeId")
    public Optional<EmployeeResponse> findByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Employee not found with id: " + id));
        return convertToDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByEmployeeId(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> ServiceException.notFound("Employee not found with employeeId: " + employeeId));
        return convertToDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByHospital(UUID hospitalId) {
        return employeeRepository.findByHospitalAndRole(hospitalId, null)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByRole(EmployeeRole role) {
        return employeeRepository.findByRoleAndIsActiveTrue(role)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "employees", key = "#id")
    public EmployeeDto updateEmployee(UUID id, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Employee not found with id: " + id));

        // Update allowed fields
        if (employeeDto.getName() != null) {
            employee.setName(employeeDto.getName());
        }
        if (employeeDto.getEmail() != null) {
            employee.setEmail(employeeDto.getEmail());
        }
        if (employeeDto.getShiftSchedule() != null) {
            employee.setShiftSchedule(employeeDto.getShiftSchedule());
        }
        if (employeeDto.getIsActive() != null) {
            employee.setIsActive(employeeDto.getIsActive());
        }

        employee = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", employee.getEmployeeId());

        return convertToDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findActiveEmployees() {
        return employeeRepository.findAllActiveEmployeesOrderByHospitalAndRole()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EmployeeDto convertToDto(Employee employee) {
        List<String> wardAssignments = parseWardAssignments(employee.getShiftSchedule());

        return EmployeeDto.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .hospitalName(employee.getHospital().getName())
                .shiftSchedule(employee.getShiftSchedule())
                .isActive(employee.getIsActive())
                .lastLogin(employee.getLastLogin())
                .wardAssignments(wardAssignments)
                .build();
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

    private List<String> parseWardAssignments(String shiftSchedule) {
        if (shiftSchedule == null || shiftSchedule.trim().isEmpty()) {
            return List.of();
        }

        if ("ALL".equalsIgnoreCase(shiftSchedule.trim())) {
            return List.of("ALL");
        }

        return Arrays.stream(shiftSchedule.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}