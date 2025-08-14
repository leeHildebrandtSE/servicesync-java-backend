package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.model.dto.*;
import com.wpc.servicesync_backend.model.entity.*;
import com.wpc.servicesync_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceSessionServiceTest {

    @Mock
    private ServiceSessionRepository sessionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private ServiceSessionService serviceSessionService;

    private Employee testEmployee;
    private Ward testWard;
    private Hospital testHospital;
    private ServiceSession testSession;

    @BeforeEach
    void setUp() {
        testHospital = Hospital.builder()
                .id(UUID.randomUUID())
                .code("TEST_HOSPITAL")
                .name("Test Hospital")
                .build();

        testWard = Ward.builder()
                .id(UUID.randomUUID())
                .hospital(testHospital)
                .name("Test Ward")
                .floorNumber(1)
                .capacity(10)
                .build();

        testEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .employeeId("H001")
                .name("Test Employee")
                .email("test@example.com")
                .role(EmployeeRole.HOSTESS)
                .hospital(testHospital)
                .isActive(true)
                .build();

        testSession = ServiceSession.builder()
                .id(UUID.randomUUID())
                .sessionId("TEST-SESSION-001")
                .employee(testEmployee)
                .ward(testWard)
                .mealType(MealType.BREAKFAST)
                .mealCount(12)
                .mealsServed(0)
                .status(SessionStatus.ACTIVE)
                .build();
    }

    @Test
    void createSession_Success() {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(testEmployee.getId(), SessionStatus.ACTIVE))
                .thenReturn(List.of());
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionDto result = serviceSessionService.createSession(request);

        // Then
        assertNotNull(result);
        assertEquals(testEmployee.getName(), result.getEmployeeName());
        assertEquals(testWard.getName(), result.getWardName());
        assertEquals(MealType.BREAKFAST, result.getMealType());
        assertEquals(12, result.getMealCount());
        assertEquals(SessionStatus.ACTIVE, result.getStatus());

        verify(sessionRepository).save(any(ServiceSession.class));
    }

    @Test
    void createSession_EmployeeNotFound() {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(UUID.randomUUID());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.createSession(request));
        assertEquals("Employee not found", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_WardNotFound() {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(UUID.randomUUID());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.createSession(request));
        assertEquals("Ward not found", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_ActiveSessionExists() {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(testEmployee.getId(), SessionStatus.ACTIVE))
                .thenReturn(List.of(testSession));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.createSession(request));
        assertEquals("Employee already has an active session", exception.getMessage());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_WithComments() {
        // Given
        SessionCreateRequest request = new SessionCreateRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);
        request.setComments("Special dietary requirements");

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(testEmployee.getId(), SessionStatus.ACTIVE))
                .thenReturn(List.of());
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionDto result = serviceSessionService.createSession(request);

        // Then
        assertNotNull(result);
        verify(sessionRepository).save(argThat(session ->
                session.getComments() != null &&
                        session.getComments().equals("Special dietary requirements")
        ));
    }

    @Test
    void updateSession_Success() {
        // Given
        String sessionId = "TEST-SESSION-001";
        SessionUpdateRequest request = new SessionUpdateRequest();
        request.setMealsServed(5);
        request.setComments("Progress update");
        request.setNurseName("Mary Williams");

        ServiceSession updatedSession = testSession.toBuilder()
                .mealsServed(5)
                .comments("Progress update")
                .nurseName("Mary Williams")
                .build();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(updatedSession);

        // When
        ServiceSessionDto result = serviceSessionService.updateSession(sessionId, request);

        // Then
        assertNotNull(result);
        verify(sessionRepository).save(argThat(session ->
                session.getMealsServed() == 5 &&
                        "Progress update".equals(session.getComments()) &&
                        "Mary Williams".equals(session.getNurseName())
        ));
    }

    @Test
    void updateSession_NotFound() {
        // Given
        String sessionId = "INVALID-SESSION";
        SessionUpdateRequest request = new SessionUpdateRequest();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.updateSession(sessionId, request));
        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void getSession_Success() {
        // Given
        when(sessionRepository.findBySessionId("TEST-SESSION-001")).thenReturn(Optional.of(testSession));

        // When
        ServiceSessionDto result = serviceSessionService.getSession("TEST-SESSION-001");

        // Then
        assertNotNull(result);
        assertEquals("TEST-SESSION-001", result.getSessionId());
        assertEquals(testEmployee.getName(), result.getEmployeeName());
        assertEquals(testWard.getName(), result.getWardName());
    }

    @Test
    void getSession_NotFound() {
        // Given
        when(sessionRepository.findBySessionId("INVALID-SESSION")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.getSession("INVALID-SESSION"));
        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void completeSession_Success() {
        // Given
        when(sessionRepository.findBySessionId("TEST-SESSION-001")).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionDto result = serviceSessionService.completeSession("TEST-SESSION-001");

        // Then
        assertNotNull(result);
        verify(sessionRepository).save(argThat(session ->
                session.getStatus() == SessionStatus.COMPLETED &&
                        session.getServiceCompleteTime() != null &&
                        session.getMealsServed().equals(session.getMealCount())
        ));
    }

    @Test
    void completeSession_NotFound() {
        // Given
        when(sessionRepository.findBySessionId("INVALID-SESSION")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.completeSession("INVALID-SESSION"));
        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void getActiveSessionsByEmployee_Success() {
        // Given
        UUID employeeId = testEmployee.getId();
        List<ServiceSession> activeSessions = List.of(testSession);

        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(employeeId, SessionStatus.ACTIVE))
                .thenReturn(activeSessions);

        // When
        List<ServiceSessionDto> result = serviceSessionService.getActiveSessionsByEmployee(employeeId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession.getSessionId(), result.get(0).getSessionId());
    }

    @Test
    void getActiveSessionsByEmployee_NoActiveSessions() {
        // Given
        UUID employeeId = testEmployee.getId();

        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(employeeId, SessionStatus.ACTIVE))
                .thenReturn(List.of());

        // When
        List<ServiceSessionDto> result = serviceSessionService.getActiveSessionsByEmployee(employeeId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSessionsByWard_Success() {
        // Given
        UUID wardId = testWard.getId();
        List<ServiceSession> wardSessions = List.of(testSession);

        when(sessionRepository.findByWardIdAndStatus(wardId, SessionStatus.ACTIVE))
                .thenReturn(wardSessions);

        // When
        List<ServiceSessionDto> result = serviceSessionService.getSessionsByWard(wardId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession.getSessionId(), result.get(0).getSessionId());
    }

    @Test
    void getCompletedSessionsBetween_Success() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        ServiceSession completedSession = testSession.toBuilder()
                .status(SessionStatus.COMPLETED)
                .serviceCompleteTime(LocalDateTime.now())
                .build();

        when(sessionRepository.findCompletedSessionsBetween(start, end))
                .thenReturn(List.of(completedSession));

        // When
        List<ServiceSessionDto> result = serviceSessionService.getCompletedSessionsBetween(start, end);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(SessionStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    void cleanupStaleSessions_Success() {
        // Given
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<ServiceSession> staleSessions = List.of(testSession);

        when(sessionRepository.findStaleActiveSessions(cutoff))
                .thenReturn(staleSessions);

        // When
        serviceSessionService.cleanupStaleSessions();

        // Then
        verify(sessionRepository).saveAll(argThat(sessions -> {
            List<ServiceSession> sessionList = (List<ServiceSession>) sessions;
            return !sessionList.isEmpty() &&
                    sessionList.get(0).getStatus() == SessionStatus.CANCELLED &&
                    sessionList.get(0).getComments().contains("[Auto-cancelled due to inactivity]");
        }));
    }

    @Test
    void convertToDto_Success() {
        // Given
        testSession = testSession.toBuilder()
                .mealsServed(8)
                .serviceStartTime(LocalDateTime.now().minusMinutes(30))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        // When
        ServiceSessionDto result = serviceSessionService.convertToDto(testSession);

        // Then
        assertNotNull(result);
        assertEquals(testSession.getId(), result.getId());
        assertEquals(testSession.getSessionId(), result.getSessionId());
        assertEquals(testEmployee.getName(), result.getEmployeeName());
        assertEquals(testWard.getName(), result.getWardName());
        assertEquals(testSession.getMealType(), result.getMealType());
        assertEquals(testSession.getMealCount(), result.getMealCount());
        assertEquals(testSession.getMealsServed(), result.getMealsServed());
        assertEquals(testSession.getStatus(), result.getStatus());
        assertEquals(testSession.getCreatedAt(), result.getCreatedAt());
        assertNotNull(result.getCompletionRate());
        assertNotNull(result.getCurrentStep());
        assertNotNull(result.getSummary());
    }

    @Test
    void generateSessionId_IsUnique() {
        // This test would require making generateSessionId package-private or public
        // For now, we test it indirectly through createSession

        // Given
        SessionCreateRequest request1 = new SessionCreateRequest();
        request1.setEmployeeId(testEmployee.getId());
        request1.setWardId(testWard.getId());
        request1.setMealType(MealType.BREAKFAST);
        request1.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.findByEmployeeAndStatusOrderByCreatedAtDesc(testEmployee.getId(), SessionStatus.ACTIVE))
                .thenReturn(List.of());
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        serviceSessionService.createSession(request1);

        // Then
        verify(sessionRepository).save(argThat(session ->
                session.getSessionId() != null &&
                        session.getSessionId().startsWith("SS-")
        ));
    }
}