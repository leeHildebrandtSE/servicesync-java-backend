package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDto {
    private String sessionId;
    private String employeeName;
    private String wardName;
    private MealType mealType;
    private Integer mealCount;
    private Double completionRate;
    private Long totalDurationMinutes;
    private String efficiencyRating;
    private LocalDateTime createdAt;
}