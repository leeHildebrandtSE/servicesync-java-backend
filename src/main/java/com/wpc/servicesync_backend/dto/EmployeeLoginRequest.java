// src/main/java/com/wpc/servicesync_backend/dto/EmployeeLoginRequest.java
package com.wpc.servicesync_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeLoginRequest {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Password is required")
    private String password;
}