// src/main/java/com/wpc/servicesync_backend/dto/DashboardStatsResponse.java
package com.wpc.servicesync_backend.dto;

import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    private Integer activeSessions;
    private Integer completedSessionsToday;
    private Integer totalMealsServedToday;
    private Double averageCompletionRate;
    private Double averageServingTime;
    private List<SessionInProgressResponse> sessionsInProgress;
    private List<SessionAwaitingResponse> sessionsAwaitingNurse;
    private Map<String, Integer> mealTypeBreakdown;
    private Map<String, Integer> wardActivityBreakdown;
}