package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.model.dto.AuthenticationRequest;
import com.wpc.servicesync_backend.model.dto.AuthenticationResponse;
import com.wpc.servicesync_backend.model.dto.EmployeeDto;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import com.wpc.servicesync_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmployeeId(),
                        request.getPassword()
                )
        );

        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Update last login
        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);

        String accessToken = jwtService.generateToken(employee);
        String refreshToken = jwtService.generateRefreshToken(employee);

        EmployeeDto employeeDto = employeeService.convertToDto(employee);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .employee(employeeDto)
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        String employeeId = jwtService.extractUsername(refreshToken);
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (jwtService.isTokenValid(refreshToken, employee)) {
            String newAccessToken = jwtService.generateToken(employee);
            EmployeeDto employeeDto = employeeService.convertToDto(employee);

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .employee(employeeDto)
                    .build();
        }

        throw new RuntimeException("Invalid refresh token");
    }
}