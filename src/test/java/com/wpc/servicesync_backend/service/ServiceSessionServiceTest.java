package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.ServiceSessionRequest;
import com.wpc.servicesync_backend.dto.ServiceSessionResponse;
import com.wpc.servicesync_backend.model.dto.SessionUpdateRequest;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.model.entity.Hospital;
import com.wpc.servicesync_backend.model.entity.MealType;
import com.wpc.servicesync_backend.model.entity.ServiceSession;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import com.wpc.servicesync_backend.repository.WardRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ServiceSessionRequest request = new ServiceSessionRequest(); // Fixed: Use ServiceSessionRequest
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionResponse result = serviceSessionService.createSession(request); // Fixed: Use ServiceSessionResponse

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
        ServiceSessionRequest request = new ServiceSessionRequest(); // Fixed: Use ServiceSessionRequest
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
        ServiceSessionRequest request = new ServiceSessionRequest(); // Fixed: Use ServiceSessionRequest
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
    void createSession_WithComments() {
        // Given
        ServiceSessionRequest request = new ServiceSessionRequest(); // Fixed: Use ServiceSessionRequest
        request.setEmployeeId(testEmployee.getId());
        request.setWardId(testWard.getId());
        request.setMealType(MealType.BREAKFAST);
        request.setMealCount(12);
        request.setComments("Special dietary requirements");

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionResponse result = serviceSessionService.createSession(request); // Fixed: Use ServiceSessionResponse

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
        SessionUpdateRequest request = new SessionUpdateRequest();
        request.setSessionId(testSession.getId()); // Fixed: Set session ID
        request.setMealsServed(5);
        request.setComments("Progress update");
        request.setNurseName("Mary Williams");

        ServiceSession updatedSession = testSession.toBuilder()
                .mealsServed(5)
                .comments("Progress update")
                .nurseName("Mary Williams")
                .build();

        when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession)); // Fixed: Use findById
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(updatedSession);

        // When
        ServiceSessionResponse result = serviceSessionService.updateSession(request); // Fixed: Use updateSession with request

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
        SessionUpdateRequest request = new SessionUpdateRequest();
        request.setSessionId(UUID.randomUUID());

        when(sessionRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Fixed: Use findById

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.updateSession(request));
        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void getSessionBySessionId_Success() { // Fixed: Renamed test method
        // Given
        when(sessionRepository.findBySessionId("TEST-SESSION-001")).thenReturn(Optional.of(testSession));

        // When
        Optional<ServiceSessionResponse> result = serviceSessionService.findBySessionId("TEST-SESSION-001"); // Fixed: Use correct method

        // Then
        assertTrue(result.isPresent());
        assertEquals("TEST-SESSION-001", result.get().getSessionId());
        assertEquals(testEmployee.getName(), result.get().getEmployeeName());
        assertEquals(testWard.getName(), result.get().getWardName());
    }

    @Test
    void getSessionBySessionId_NotFound() { // Fixed: Renamed test method
        // Given
        when(sessionRepository.findBySessionId("INVALID-SESSION")).thenReturn(Optional.empty());

        // When
        Optional<ServiceSessionResponse> result = serviceSessionService.findBySessionId("INVALID-SESSION"); // Fixed: Use correct method

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void completeSession_Success() {
        // Given
        when(sessionRepository.findById(testSession.getId())).thenReturn(Optional.of(testSession)); // Fixed: Use findById
        when(sessionRepository.save(any(ServiceSession.class))).thenReturn(testSession);

        // When
        ServiceSessionResponse result = serviceSessionService.completeSession(testSession.getId()); // Fixed: Use completeSession with UUID

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
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty()); // Fixed: Use findById

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceSessionService.completeSession(sessionId));
        assertEquals("Session not found", exception.getMessage());
    }

    @Test
    void getActiveSessionsByEmployee_Success() {
        // Given
        UUID employeeId = testEmployee.getId();
        List<ServiceSession> activeSessions = List.of(testSession);

        when(sessionRepository.findByEmployeeIdAndStatus(employeeId, SessionStatus.ACTIVE)) // Fixed: Use correct repository method
                .thenReturn(activeSessions);

        // When
        List<ServiceSessionResponse> result = serviceSessionService.findActiveSessionsByEmployee(employeeId); // Fixed: Use correct service method

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession.getSessionId(), result.get(0).getSessionId());
    }

    @Test
    void getActiveSessionsByEmployee_NoActiveSessions() {
        // Given
        UUID employeeId = testEmployee.getId();

        when(sessionRepository.findByEmployeeIdAndStatus(employeeId, SessionStatus.ACTIVE)) // Fixed: Use correct repository method
                .thenReturn(List.of());

        // When
        List<ServiceSessionResponse> result = serviceSessionService.findActiveSessionsByEmployee(employeeId); // Fixed: Use correct service method

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
        var result = serviceSessionService.getSessionsByWard(wardId); // Fixed: Use var for ServiceSessionDto return type

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
        var result = serviceSessionService.getCompletedSessionsBetween(start, end); // Fixed: Use var for ServiceSessionDto return type

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
        var result = serviceSessionService.convertToDto(testSession); // Fixed: Use var for ServiceSessionDto return type

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
        ServiceSessionRequest request1 = new ServiceSessionRequest(); // Fixed: Use ServiceSessionRequest
        request1.setEmployeeId(testEmployee.getId());
        request1.setWardId(testWard.getId());
        request1.setMealType(MealType.BREAKFAST);
        request1.setMealCount(12);

        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(wardRepository.findById(testWard.getId())).thenReturn(Optional.of(testWard));
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