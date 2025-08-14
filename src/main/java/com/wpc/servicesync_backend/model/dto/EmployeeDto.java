package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private UUID id;
    private String employeeId;
    private String name;
    private String email;
    private EmployeeRole role;
    private String hospitalName;
    private String shiftSchedule;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private List<String> wardAssignments;
}