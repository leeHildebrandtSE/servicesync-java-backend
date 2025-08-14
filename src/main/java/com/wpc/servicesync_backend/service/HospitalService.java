// src/main/java/com/wpc/servicesync_backend/service/HospitalService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.HospitalResponse;
import com.wpc.servicesync_backend.dto.WardResponse;
import com.wpc.servicesync_backend.exception.ServiceException;
import com.wpc.servicesync_backend.model.entity.Hospital;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.repository.HospitalRepository;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final ServiceSessionRepository sessionRepository;

    @Cacheable("hospitals")
    public List<HospitalResponse> getAllActiveHospitals() {
        log.info("Fetching all active hospitals");
        return hospitalRepository.findActiveHospitalsOrderByName()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HospitalResponse getHospitalById(UUID id) {
        log.info("Fetching hospital by ID: {}", id);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Hospital not found with id: " + id));
        return mapToResponse(hospital);
    }

    public Map<String, Object> getHospitalStats(UUID hospitalId) {
        log.info("Fetching statistics for hospital: {}", hospitalId);

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> ServiceException.notFound("Hospital not found with id: " + hospitalId));

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // Get recent sessions for this hospital
        var recentSessions = sessionRepository.findRecentSessionsByHospital(hospitalId, startOfDay);

        // Calculate statistics
        int totalSessionsToday = recentSessions.size();
        long completedSessionsToday = recentSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .count();

        int totalMealsServedToday = recentSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .mapToInt(s -> s.getMealsServed())
                .sum();

        double averageCompletionRate = recentSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .mapToDouble(s -> s.getCompletionRate())
                .average()
                .orElse(0.0);

        return Map.of(
                "hospitalName", hospital.getName(),
                "hospitalCode", hospital.getCode(),
                "date", LocalDateTime.now(),
                "totalSessionsToday", totalSessionsToday,
                "completedSessionsToday", completedSessionsToday,
                "totalMealsServedToday", totalMealsServedToday,
                "averageCompletionRate", Math.round(averageCompletionRate * 100) / 100.0,
                "activeWards", hospital.getWards() != null ?
                        hospital.getWards().stream().filter(w -> w.getIsActive()).count() : 0,
                "activeEmployees", hospital.getEmployees() != null ?
                        hospital.getEmployees().stream().filter(e -> e.getIsActive()).count() : 0
        );
    }

    private HospitalResponse mapToResponse(Hospital hospital) {
        List<WardResponse> wards = hospital.getWards() != null ?
                hospital.getWards().stream()
                        .filter(ward -> ward.getIsActive())
                        .map(ward -> WardResponse.builder()
                                .id(ward.getId())
                                .name(ward.getName())
                                .floorNumber(ward.getFloorNumber())
                                .capacity(ward.getCapacity())
                                .hospitalName(hospital.getName())
                                .hospitalCode(hospital.getCode())
                                .displayName(ward.getDisplayName())
                                .isActive(ward.getIsActive())
                                .activeSessionsCount(0) // Could be calculated if needed
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return HospitalResponse.builder()
                .id(hospital.getId())
                .code(hospital.getCode())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contactEmail(hospital.getContactEmail())
                .contactPhone(hospital.getContactPhone())
                .isActive(hospital.getIsActive())
                .activeWardsCount(wards.size())
                .activeEmployeesCount(hospital.getEmployees() != null ?
                        (int) hospital.getEmployees().stream().filter(e -> e.getIsActive()).count() : 0)
                .wards(wards)
                .build();
    }
}