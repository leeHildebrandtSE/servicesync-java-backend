package com.wpc.servicesync_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDto {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
    private List<WardDto> wards;
    private Integer employeeCount;
}