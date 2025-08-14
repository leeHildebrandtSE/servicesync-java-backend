// src/main/java/com/wpc/servicesync_backend/dto/HospitalResponse.java
package com.wpc.servicesync_backend.dto;

import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HospitalResponse {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
    private Integer activeWardsCount;
    private Integer activeEmployeesCount;
    private List<WardResponse> wards;
}