// src/main/java/com/wpc/servicesync_backend/dto/ServiceSessionResponse.java
package com.wpc.servicesync_backend.dto;

import com.wpc.servicesync_backend.model.entity.MealType;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ServiceSessionResponse {
    private UUID id;
    private String sessionId;
    private String employeeName;
    private String employeeId;
    private String wardName;
    private String hospitalName;
    private MealType mealType;
    private Integer mealCount;
    private Integer mealsServed;
    private SessionStatus status;
    private Double completionRate;
    private String currentStep;
    private String efficiencyRating;

    // Timestamps
    private LocalDateTime kitchenExitTime;
    private LocalDateTime wardArrivalTime;
    private LocalDateTime nurseAlertTime;
    private LocalDateTime nurseResponseTime;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceCompleteTime;

    // Duration calculations (in seconds)
    private Long travelTimeSeconds;
    private Long nurseResponseTimeSeconds;
    private Long servingTimeSeconds;
    private Long totalDurationSeconds;

    // Additional fields
    private String comments;
    private String nurseName;
    private Boolean dietSheetDocumented;
    private String dietSheetNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}