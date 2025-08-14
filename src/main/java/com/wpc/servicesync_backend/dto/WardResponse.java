// src/main/java/com/wpc/servicesync_backend/dto/WardResponse.java
package com.wpc.servicesync_backend.dto;

import lombok.Data;
import lombok.Builder;

import java.util.UUID;

@Data
@Builder
public class WardResponse {
    private UUID id;
    private String name;
    private Integer floorNumber;
    private Integer capacity;
    private String hospitalName;
    private String hospitalCode;
    private String displayName;
    private Boolean isActive;
    private Integer activeSessionsCount;
}