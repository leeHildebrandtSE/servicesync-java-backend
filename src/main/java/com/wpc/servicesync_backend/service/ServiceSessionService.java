package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.QRScanRequest;
import com.wpc.servicesync_backend.dto.ServiceSessionRequest;
import com.wpc.servicesync_backend.dto.ServiceSessionResponse;
import com.wpc.servicesync_backend.model.dto.ServiceSessionDto;
import com.wpc.servicesync_backend.model.dto.SessionUpdateRequest;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.QRLocationType;
import com.wpc.servicesync_backend.model.entity.ServiceSession;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.repository.EmployeeRepository;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import com.wpc.servicesync_backend.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceSessionService {

    private final ServiceSessionRepository sessionRepository;
    private final EmployeeRepository employeeRepository;
    private final WardRepository wardRepository;

    public ServiceSessionResponse createSession(ServiceSessionRequest request) {
        log.info("Creating new service session for employee: {} and ward: {}",
                request.getEmployeeId(), request.getWardId());

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        // Generate unique session ID
        String sessionId = generateSessionId(employee, ward);

        ServiceSession session = ServiceSession.builder()
                .sessionId(sessionId)
                .employee(employee)
                .ward(ward)
                .mealType(request.getMealType())
                .mealCount(request.getMealCount())
                .mealsServed(0)
                .status(SessionStatus.ACTIVE)
                .comments(request.getComments())
                .dietSheetDocumented(false)
                .build();

        session = sessionRepository.save(session);
        log.info("Service session created with ID: {}", session.getSessionId());

        return mapToResponse(session);
    }

    public ServiceSessionResponse scanQR(QRScanRequest request) {
        log.info("Processing QR scan for session: {} at location: {}",
                request.getSessionId(), request.getLocationType());

        ServiceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Validate QR code format
        if (!validateQRCode(request.getQrCodeContent(), request.getLocationType())) {
            throw new RuntimeException("Invalid QR code for location type: " + request.getLocationType());
        }

        LocalDateTime now = LocalDateTime.now();

        // Update session based on location type
        switch (request.getLocationType()) {
            case KITCHEN_EXIT -> {
                session.setKitchenExitTime(now);
                session.setStatus(SessionStatus.IN_TRANSIT);
                log.info("Kitchen exit recorded for session: {}", session.getSessionId());
            }
            case WARD_ARRIVAL -> {
                session.setWardArrivalTime(now);
                log.info("Ward arrival recorded for session: {}", session.getSessionId());
            }
            case NURSE_STATION -> {
                session.setServiceStartTime(now);
                log.info("Service start recorded for session: {}", session.getSessionId());
            }
        }

        session = sessionRepository.save(session);
        return mapToResponse(session);
    }

    public ServiceSessionResponse alertNurse(UUID sessionId) {
        log.info("Sending nurse alert for session: {}", sessionId);

        ServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setNurseAlertTime(LocalDateTime.now());
        session = sessionRepository.save(session);

        log.info("Nurse alert sent for session: {}", session.getSessionId());
        return mapToResponse(session);
    }

    public ServiceSessionResponse nurseResponse(UUID sessionId, String nurseName) {
        log.info("Recording nurse response for session: {} by nurse: {}", sessionId, nurseName);

        ServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setNurseResponseTime(LocalDateTime.now());
        session.setNurseName(nurseName);
        session = sessionRepository.save(session);

        log.info("Nurse response recorded for session: {}", session.getSessionId());
        return mapToResponse(session);
    }

    public ServiceSessionResponse updateSession(SessionUpdateRequest request) {
        log.info("Updating session: {}", request.getSessionId());

        ServiceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (request.getMealsServed() != null) {
            session.setMealsServed(request.getMealsServed());
        }
        if (request.getComments() != null) {
            session.setComments(request.getComments());
        }
        if (request.getNurseName() != null) {
            session.setNurseName(request.getNurseName());
        }
        if (request.getDietSheetDocumented() != null) {
            session.setDietSheetDocumented(request.getDietSheetDocumented());
        }
        if (request.getDietSheetNotes() != null) {
            session.setDietSheetNotes(request.getDietSheetNotes());
        }
        if (request.getDietSheetPhotoPath() != null) {
            session.setDietSheetPhotoPath(request.getDietSheetPhotoPath());
        }

        session = sessionRepository.save(session);
        log.info("Session updated: {}", session.getSessionId());

        return mapToResponse(session);
    }

    public ServiceSessionResponse completeSession(UUID sessionId) {
        log.info("Completing session: {}", sessionId);

        ServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setServiceCompleteTime(LocalDateTime.now());
        session.setStatus(SessionStatus.COMPLETED);

        // Set meals served to meal count if not already set
        if (session.getMealsServed() < session.getMealCount()) {
            session.setMealsServed(session.getMealCount());
        }

        session = sessionRepository.save(session);
        log.info("Session completed: {}", session.getSessionId());

        return mapToResponse(session);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceSessionResponse> findBySessionId(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ServiceSessionResponse> findActiveSessionsByEmployee(UUID employeeId) {
        return sessionRepository.findByEmployeeIdAndStatus(employeeId, SessionStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceSessionResponse> findRecentSessionsByEmployee(UUID employeeId, LocalDateTime since) {
        return sessionRepository.findRecentSessionsByEmployee(employeeId, since)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServiceSessionResponse> findCompletedSessions(Pageable pageable) {
        return sessionRepository.findByStatusOrderByCreatedAtDesc(SessionStatus.COMPLETED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ServiceSessionResponse> findAllActiveSessions() {
        return sessionRepository.findAllActiveSessions()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Additional methods for cleanup and advanced features
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupStaleSessions() {
        log.info("Starting cleanup of stale sessions");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<ServiceSession> staleSessions = sessionRepository.findStaleActiveSessions(cutoff);

        if (!staleSessions.isEmpty()) {
            staleSessions.forEach(session -> {
                session.setStatus(SessionStatus.CANCELLED);
                String existingComments = session.getComments() != null ? session.getComments() : "";
                session.setComments(existingComments + " [Auto-cancelled due to inactivity]");
            });

            sessionRepository.saveAll(staleSessions);
            log.info("Cancelled {} stale sessions", staleSessions.size());
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceSessionDto> getSessionsByWard(UUID wardId) {
        return sessionRepository.findByWardIdAndStatus(wardId, SessionStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceSessionDto> getCompletedSessionsBetween(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findCompletedSessionsBetween(start, end)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private String generateSessionId(Employee employee, Ward ward) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "SS-" + employee.getEmployeeId() + "-" + ward.getName() + "-" + timestamp;
    }

    private boolean validateQRCode(String qrCode, QRLocationType locationType) {
        // Simple validation - check if QR code starts with expected prefix
        return qrCode.startsWith(locationType.getPrefix());
    }

    private ServiceSessionResponse mapToResponse(ServiceSession session) {
        return ServiceSessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .employeeId(session.getEmployee().getEmployeeId())
                .wardName(session.getWard().getName())
                .hospitalName(session.getWard().getHospital().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .mealsServed(session.getMealsServed())
                .status(session.getStatus())
                .completionRate(session.getCompletionRate())
                .currentStep(session.getCurrentStep())
                .efficiencyRating(session.getEfficiencyRating())
                .kitchenExitTime(session.getKitchenExitTime())
                .wardArrivalTime(session.getWardArrivalTime())
                .nurseAlertTime(session.getNurseAlertTime())
                .nurseResponseTime(session.getNurseResponseTime())
                .serviceStartTime(session.getServiceStartTime())
                .serviceCompleteTime(session.getServiceCompleteTime())
                // Fixed: Convert milliseconds to seconds properly
                .travelTimeSeconds(session.getTravelTime() / 1000)
                .nurseResponseTimeSeconds(session.getNurseResponseTime() / 1000)
                .servingTimeSeconds(session.getServingTime() / 1000)
                .totalDurationSeconds(session.getElapsedTime() / 1000)
                .comments(session.getComments())
                .nurseName(session.getNurseName())
                .dietSheetDocumented(session.getDietSheetDocumented())
                .dietSheetNotes(session.getDietSheetNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    public ServiceSessionDto convertToDto(ServiceSession session) {
        return ServiceSessionDto.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .wardName(session.getWard().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .mealsServed(session.getMealsServed())
                .status(session.getStatus())
                .kitchenExitTime(session.getKitchenExitTime())
                .wardArrivalTime(session.getWardArrivalTime())
                .nurseAlertTime(session.getNurseAlertTime())
                .nurseResponseTime(session.getNurseResponseTime())
                .serviceStartTime(session.getServiceStartTime())
                .serviceCompleteTime(session.getServiceCompleteTime())
                .comments(session.getComments())
                .nurseName(session.getNurseName())
                .dietSheetDocumented(session.getDietSheetDocumented())
                .dietSheetNotes(session.getDietSheetNotes())
                .createdAt(session.getCreatedAt())
                .completionRate((double) session.getCompletionRate())
                .currentStep(session.getCurrentStep())
                .summary(session.getSummary())
                .build();
    }
}