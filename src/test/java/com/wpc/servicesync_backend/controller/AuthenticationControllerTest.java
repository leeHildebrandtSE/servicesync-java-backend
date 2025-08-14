package com.wpc.servicesync_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpc.servicesync_backend.model.dto.AuthenticationRequest;
import com.wpc.servicesync_backend.model.dto.AuthenticationResponse;
import com.wpc.servicesync_backend.model.dto.EmployeeDto;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticate_Success() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmployeeId("H001");
        request.setPassword("password123");

        EmployeeDto employeeDto = EmployeeDto.builder()
                .id(UUID.randomUUID())
                .employeeId("H001")
                .name("Test Employee")
                .role(EmployeeRole.HOSTESS)
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .employee(employeeDto)
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.employee.employeeId").value("H001"));
    }

    @Test
    void authenticate_InvalidCredentials() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmployeeId("INVALID");
        request.setPassword("wrong-password");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void refreshToken_Success() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";

        EmployeeDto employeeDto = EmployeeDto.builder()
                .id(UUID.randomUUID())
                .employeeId("H001")
                .name("Test Employee")
                .role(EmployeeRole.HOSTESS)
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("new-access-token")
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .employee(employeeDto)
                .build();

        when(authenticationService.refreshToken(refreshToken)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }
}