// src/main/java/com/wpc/servicesync_backend/service/WardService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.ServiceSessionResponse;
import com.wpc.servicesync_backend.dto.WardResponse;
import com.wpc.servicesync_backend.exception.ServiceException;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import com.wpc.servicesync_backend.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WardService {

    private final WardRepository wardRepository;
    private final ServiceSessionRepository sessionRepository;
    private final ServiceSessionService sessionService;

    @Cacheable(value = "wards", key = "#hospitalId")
    public List<WardResponse> getWardsByHospital(UUID hospitalId) {
        log.info("Fetching wards for hospital: {}", hospitalId);
        return wardRepository.findByHospital_IdAndIsActiveTrue(hospitalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public WardResponse getWardById(UUID id) {
        log.info("Fetching ward by ID: {}", id);
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Ward not found with id: " + id));
        return mapToResponse(ward);
    }

    @Cacheable("wards")
    public List<WardResponse> getAllActiveWards() {
        log.info("Fetching all active wards");
        return wardRepository.findAllActiveWardsOrderByHospitalAndName()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getWardSessions(UUID wardId) {
        log.info("Fetching sessions for ward: {}", wardId);

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> ServiceException.notFound("Ward not found with id: " + wardId));

        // Get active sessions for this ward
        List<ServiceSessionResponse> activeSessions = sessionRepository.findByWardIdAndStatus(wardId, SessionStatus.ACTIVE)
                .stream()
                .map(session -> ServiceSessionResponse.builder()
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
                        .createdAt(session.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Get sessions awaiting nurse response
        List<ServiceSessionResponse> awaitingNurse = sessionRepository.findSessionsAwaitingNurseResponse()
                .stream()
                .filter(session -> session.getWard().getId().equals(wardId))
                .map(session -> ServiceSessionResponse.builder()
                        .id(session.getId())
                        .sessionId(session.getSessionId())
                        .employeeName(session.getEmployee().getName())
                        .wardName(session.getWard().getName())
                        .mealType(session.getMealType())
                        .mealCount(session.getMealCount())
                        .nurseAlertTime(session.getNurseAlertTime())
                        .currentStep(session.getCurrentStep())
                        .build())
                .collect(Collectors.toList());

        return Map.of(
                "wardName", ward.getName(),
                "hospitalName", ward.getHospital().getName(),
                "capacity", ward.getCapacity(),
                "floorNumber", ward.getFloorNumber() != null ? ward.getFloorNumber() : 0,
                "activeSessions", activeSessions,
                "sessionsAwaitingNurse", awaitingNurse,
                "totalActiveSessions", activeSessions.size()
        );
    }

    private WardResponse mapToResponse(Ward ward) {
        // Calculate active sessions count
        int activeSessionsCount = sessionRepository.findByWardIdAndStatus(ward.getId(), SessionStatus.ACTIVE).size();

        return WardResponse.builder()
                .id(ward.getId())
                .name(ward.getName())
                .floorNumber(ward.getFloorNumber())
                .capacity(ward.getCapacity())
                .hospitalName(ward.getHospital().getName())
                .hospitalCode(ward.getHospital().getCode())
                .displayName(ward.getDisplayName())
                .isActive(ward.getIsActive())
                .activeSessionsCount(activeSessionsCount)
                .build();
    }
}