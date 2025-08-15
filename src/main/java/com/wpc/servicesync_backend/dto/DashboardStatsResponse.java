package com.wpc.servicesync_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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