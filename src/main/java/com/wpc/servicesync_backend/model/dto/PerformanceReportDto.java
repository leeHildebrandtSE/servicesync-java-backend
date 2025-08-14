package com.wpc.servicesync_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReportDto {
    private LocalDateTime reportDate;
    private String reportPeriod;
    private Integer totalSessions;
    private Integer completedSessions;
    private Double averageCompletionRate;
    private Long averageTravelTimeMinutes;
    private Long averageNurseResponseTimeMinutes;
    private Long averageServingTimeMinutes;
    private Double averageServingRate;
    private String efficiencyRating;
    private List<SessionSummaryDto> topPerformingSessions;
    private List<SessionSummaryDto> problematicSessions;
}