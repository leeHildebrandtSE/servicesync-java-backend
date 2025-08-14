package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.MealType;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSessionDto {
    private UUID id;
    private String sessionId;
    private String employeeName;
    private String wardName;
    private MealType mealType;
    private Integer mealCount;
    private Integer mealsServed;
    private SessionStatus status;
    private LocalDateTime kitchenExitTime;
    private LocalDateTime wardArrivalTime;
    private LocalDateTime nurseAlertTime;
    private LocalDateTime nurseResponseTime;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceCompleteTime;
    private String comments;
    private String nurseName;
    private Boolean dietSheetDocumented;
    private String dietSheetNotes;
    private LocalDateTime createdAt;
    private Double completionRate;
    private String currentStep;
    private String summary;
}