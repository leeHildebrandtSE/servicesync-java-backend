package com.wpc.servicesync_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;

    @Builder.Default  // Fixed: Added @Builder.Default to handle the default value properly
    private String tokenType = "Bearer";

    private Long expiresIn;
    private EmployeeDto employee;
}