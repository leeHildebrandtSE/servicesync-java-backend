// src/main/java/com/wpc/servicesync_backend/service/HospitalService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.HospitalResponse;
import com.wpc.servicesync_backend.exception.ServiceException;
import com.wpc.servicesync_backend.model.entity.Hospital;
import com.wpc.servicesync_backend.repository.HospitalRepository;
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
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    @Cacheable("hospitals")
    public List<HospitalResponse> getAllActiveHospitals() {
        return hospitalRepository.findActiveHospitalsOrderByName()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HospitalResponse getHospitalById(UUID id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Hospital not found with id: " + id));
        return mapToResponse(hospital);
    }

    private HospitalResponse mapToResponse(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .code(hospital.getCode())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contactEmail(hospital.getContactEmail())
                .contactPhone(hospital.getContactPhone())
                .isActive(hospital.getIsActive())
                .activeWardsCount(hospital.getWards() != null ?
                        (int) hospital.getWards().stream().filter(w -> w.getIsActive()).count() : 0)
                .activeEmployeesCount(hospital.getEmployees() != null ?
                        (int) hospital.getEmployees().stream().filter(e -> e.getIsActive()).count() : 0)
                .build();
    }
}