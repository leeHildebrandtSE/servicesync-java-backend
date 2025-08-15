package com.wpc.servicesync_backend.dto;

import com.wpc.servicesync_backend.model.entity.MealType;
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
public class SessionInProgressResponse {
    private UUID sessionId;
    private String sessionIdString;
    private String employeeName;
    private String wardName;
    private MealType mealType;
    private Integer mealCount;
    private Integer mealsServed;
    private Double completionRate;
    private String currentStep;
    private LocalDateTime startTime;
    private Long elapsedMinutes;
}