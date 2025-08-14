package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.model.dto.AuthenticationRequest;
import com.wpc.servicesync_backend.model.dto.AuthenticationResponse;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.model.entity.Hospital;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import com.wpc.servicesync_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Employee testEmployee;
    private Hospital testHospital;

    @BeforeEach
    void setUp() {
        testHospital = Hospital.builder()
                .id(UUID.randomUUID())
                .code("TEST_HOSPITAL")
                .name("Test Hospital")
                .build();

        testEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .employeeId("H001")
                .name("Test Employee")
                .email("test@example.com")
                .role(EmployeeRole.HOSTESS)
                .hospital(testHospital)
                .passwordHash("hashedPassword")
                .isActive(true)
                .build();
    }

    @Test
    void authenticate_Success() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmployeeId("H001");
        request.setPassword("password123");

        when(employeeRepository.findByEmployeeId("H001")).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        when(jwtService.generateToken(testEmployee)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testEmployee)).thenReturn("refresh-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        when(employeeService.convertToDto(testEmployee)).thenReturn(new com.wpc.servicesync_backend.model.dto.EmployeeDto());

        // When
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).save(testEmployee);
    }

    @Test
    void authenticate_EmployeeNotFound() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmployeeId("INVALID");
        request.setPassword("password123");

        when(employeeRepository.findByEmployeeId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void refreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";

        when(jwtService.extractUsername(refreshToken)).thenReturn("H001");
        when(employeeRepository.findByEmployeeId("H001")).thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(refreshToken, testEmployee)).thenReturn(true);
        when(jwtService.generateToken(testEmployee)).thenReturn("new-access-token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);
        when(employeeService.convertToDto(testEmployee)).thenReturn(new com.wpc.servicesync_backend.model.dto.EmployeeDto());

        // When
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void refreshToken_InvalidToken() {
        // Given
        String refreshToken = "invalid-refresh-token";

        when(jwtService.extractUsername(refreshToken)).thenReturn("H001");
        when(employeeRepository.findByEmployeeId("H001")).thenReturn(Optional.of(testEmployee));
        when(jwtService.isTokenValid(refreshToken, testEmployee)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(refreshToken));
    }
}