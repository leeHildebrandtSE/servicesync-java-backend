// src/main/java/com/wpc/servicesync_backend/service/WardService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.WardResponse;
import com.wpc.servicesync_backend.exception.ServiceException;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WardService {

    private final WardRepository wardRepository;

    @Cacheable(value = "wards", key = "#hospitalId")
    public List<WardResponse> getWardsByHospital(UUID hospitalId) {
        return wardRepository.findByHospital_IdAndIsActiveTrue(hospitalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public WardResponse getWardById(UUID id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Ward not found with id: " + id));
        return mapToResponse(ward);
    }

    private WardResponse mapToResponse(Ward ward) {
        return WardResponse.builder()
                .id(ward.getId())
                .name(ward.getName())
                .floorNumber(ward.getFloorNumber())
                .capacity(ward.getCapacity())
                .hospitalName(ward.getHospital().getName())
                .hospitalCode(ward.getHospital().getCode())
                .displayName(ward.getDisplayName())
                .isActive(ward.getIsActive())
                .activeSessionsCount(ward.getServiceSessions() != null ?
                        (int) ward.getServiceSessions().stream()
                                .filter(s -> s.getStatus().name().equals("ACTIVE"))
                                .count() : 0)
                .build();
    }
}