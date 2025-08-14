// src/main/java/com/wpc/servicesync_backend/dto/EmployeeResponse.java
package com.wpc.servicesync_backend.dto;

import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EmployeeResponse {
    private UUID id;
    private String employeeId;
    private String name;
    private String email;
    private EmployeeRole role;
    private String hospitalName;
    private String hospitalCode;
    private String shiftSchedule;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}