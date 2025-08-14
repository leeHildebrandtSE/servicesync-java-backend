package com.wpc.servicesync_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpc.servicesync_backend.model.dto.*;
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
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        ServiceSessionDto responseDto = ServiceSessionDto.builder()
                .id(UUID.randomUUID())
                .sessionId("TEST-SESSION-001")
                .employeeName("Test Employee")
                .wardName("Test Ward")
                .mealType(MealType.BREAKFAST)
                .mealCount(12)
                .status(SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(sessionService.createSession(any(SessionCreateRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value("TEST-SESSION-001"))
                .andExpect(jsonPath("$.employeeName").value("Test Employee"))
                .andExpect(jsonPath("$.mealType").value("BREAKFAST"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void getSession_Success() throws Exception {
        // Given
        String sessionId = "TEST-SESSION-001";
        ServiceSessionDto responseDto = ServiceSessionDto.builder()
                .sessionId(sessionId)
                .employeeName("Test Employee")
                .wardName("Test Ward")
                .mealType(MealType.BREAKFAST)
                .mealCount(12)
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionService.getSession(sessionId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk()) // Fixed typo: was "andExpected"
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpected(jsonPath("$.employeeName").value("Test Employee"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void getActiveSessionsByEmployee_Success() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        List<ServiceSessionDto> sessions = List.of(
                ServiceSessionDto.builder()
                        .sessionId("SESSION-001")
                        .employeeName("Test Employee")
                        .status(SessionStatus.ACTIVE)
                        .build()
        );

        when(sessionService.getActiveSessionsByEmployee(employeeId)).thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/sessions/employee/{employeeId}/active", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sessionId").value("SESSION-001"));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void updateSession_Success() throws Exception {
        // Given
        String sessionId = "TEST-SESSION-001";
        SessionUpdateRequest request = new SessionUpdateRequest();
        request.setMealsServed(5);
        request.setComments("Updated progress");

        ServiceSessionDto responseDto = ServiceSessionDto.builder()
                .sessionId(sessionId)
                .employeeName("Test Employee")
                .mealsServed(5)
                .comments("Updated progress")
                .build();

        when(sessionService.updateSession(sessionId, request)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/sessions/{sessionId}", sessionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.mealsServed").value(5));
    }

    @Test
    @WithMockUser(roles = "HOSTESS")
    void completeSession_Success() throws Exception {
        // Given
        String sessionId = "TEST-SESSION-001";
        ServiceSessionDto responseDto = ServiceSessionDto.builder()
                .sessionId(sessionId)
                .status(SessionStatus.COMPLETED)
                .completionRate(100.0)
                .build();

        when(sessionService.completeSession(sessionId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/sessions/{sessionId}/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void createSession_Unauthorized() throws Exception {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        // When & Then
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "NURSE") // Nurse role shouldn't be able to create sessions
    void createSession_Forbidden() throws Exception {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        // When & Then
        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}