package com.wpc.servicesync_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpc.servicesync_backend.dto.*;
import com.wpc.servicesync_backend.model.entity.MealType;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.service.ServiceSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceSessionController.class)
class ServiceSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceSessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "HOSTESS")
    void createSession_Success() throws Exception {
        // Given
        ServiceSessionRequest request = new ServiceSessionRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        ServiceSessionResponse responseDto = ServiceSessionResponse.builder()
                .id(UUID.randomUUID())
                .sessionId("TEST-SESSION-001")
                .employeeName("Test Employee")
                .wardName("Test Ward")
                .mealType(MealType.BREAKFAST)
                .mealCount(12)
                .status(SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .completionRate(0.0)
                .currentStep("Kitchen Exit")
                .build();

        when(sessionService.createSession(any(ServiceSessionRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sessionId").value("TEST-SESSION-001"))
                .andExpect(jsonPath("$.data.employeeName").value("Test Employee"))
                .andExpect(jsonPath("$.data.mealType").value("BREAKFAST"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void getSession_Success() throws Exception {
        // Given
        String sessionId = "TEST-SESSION-001";
        ServiceSessionResponse responseDto = ServiceSessionResponse.builder()
                .sessionId(sessionId)
                .employeeName("Test Employee")
                .wardName("Test Ward")
                .mealType(MealType.BREAKFAST)
                .mealCount(12)
                .status(SessionStatus.ACTIVE)
                .completionRate(0.0)
                .currentStep("Kitchen Exit")
                .build();

        when(sessionService.findBySessionId(sessionId)).thenReturn(Optional.of(responseDto));

        // When & Then
        mockMvc.perform(get("/api/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(sessionId))
                .andExpect(jsonPath("$.data.employeeName").value("Test Employee"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void getActiveSessionsByEmployee_Success() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        List<ServiceSessionResponse> sessions = List.of(
                ServiceSessionResponse.builder()
                        .sessionId("SESSION-001")
                        .employeeName("Test Employee")
                        .status(SessionStatus.ACTIVE)
                        .completionRate(0.0)
                        .currentStep("Kitchen Exit")
                        .build()
        );

        when(sessionService.findActiveSessionsByEmployee(employeeId)).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/sessions/employee/{employeeId}/active", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].sessionId").value("SESSION-001"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void completeSession_Success() throws Exception {
        // Given
        UUID sessionId = UUID.randomUUID();
        ServiceSessionResponse responseDto = ServiceSessionResponse.builder()
                .id(sessionId)
                .sessionId("TEST-SESSION-001")
                .status(SessionStatus.COMPLETED)
                .completionRate(100.0)
                .currentStep("Service Complete")
                .build();

        when(sessionService.completeSession(sessionId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/sessions/{sessionId}/complete", sessionId)
                        .with(csrf()))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void createSession_Unauthorized() throws Exception {
        // Given
        ServiceSessionRequest request = new ServiceSessionRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        // When & Then
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized());
    }
}