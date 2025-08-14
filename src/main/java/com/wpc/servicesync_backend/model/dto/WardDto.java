package com.wpc.servicesync_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardDto {
    private UUID id;
    private String name;
    private Integer floorNumber;
    private Integer capacity;
    private String hospitalName;
    private Boolean isActive;
    private Integer activeSessionsCount;
}