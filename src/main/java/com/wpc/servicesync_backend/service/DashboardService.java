// src/main/java/com/wpc/servicesync_backend/service/DashboardService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.DashboardStatsResponse;
import com.wpc.servicesync_backend.dto.SessionAwaitingResponse;
import com.wpc.servicesync_backend.dto.SessionInProgressResponse;
import com.wpc.servicesync_backend.model.entity.ServiceSession;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final ServiceSessionRepository sessionRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        // Get basic counts
        int activeSessions = sessionRepository.findAllActiveSessions().size();
        long completedToday = sessionRepository.getCompletedSessionsCountSince(startOfDay);

        // Get sessions in progress
        List<SessionInProgressResponse> sessionsInProgress =
                sessionRepository.findSessionsInProgress()
                        .stream()
                        .map(this::mapToInProgressResponse)
                        .toList();

        // Get sessions awaiting nurse response
        List<SessionAwaitingResponse> sessionsAwaitingNurse =
                sessionRepository.findSessionsAwaitingNurseResponse()
                        .stream()
                        .map(this::mapToAwaitingResponse)
                        .toList();

        // Get statistics
        Double avgMealsServed = sessionRepository.getAverageMealsServedSince(startOfDay);

        // Get meal type breakdown
        Map<String, Integer> mealTypeBreakdown =
                sessionRepository.getMealTypeStatisticsSince(startOfDay)
                        .stream()
                        .collect(Collectors.toMap(
                                result -> result[0].toString(),
                                result -> ((Number) result[1]).intValue()
                        ));

        // Get ward activity breakdown
        Map<String, Integer> wardActivityBreakdown =
                sessionRepository.getWardActivityStatisticsSince(startOfDay)
                        .stream()
                        .collect(Collectors.toMap(
                                result -> result[0].toString(),
                                result -> ((Number) result[1]).intValue()
                        ));

        return DashboardStatsResponse.builder()
                .activeSessions(activeSessions)
                .completedSessionsToday((int) completedToday)
                .totalMealsServedToday(avgMealsServed != null ? avgMealsServed.intValue() : 0)
                .averageCompletionRate(calculateAverageCompletionRate(startOfDay))
                .averageServingTime(calculateAverageServingTime(startOfDay))
                .sessionsInProgress(sessionsInProgress)
                .sessionsAwaitingNurse(sessionsAwaitingNurse)
                .mealTypeBreakdown(mealTypeBreakdown)
                .wardActivityBreakdown(wardActivityBreakdown)
                .build();
    }

    private SessionInProgressResponse mapToInProgressResponse(ServiceSession session) {
        long elapsedMinutes = session.getKitchenExitTime() != null ?
                ChronoUnit.MINUTES.between(session.getKitchenExitTime(), LocalDateTime.now()) : 0;

        return SessionInProgressResponse.builder()
                .sessionId(session.getId())
                .sessionIdString(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .wardName(session.getWard().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .mealsServed(session.getMealsServed())
                .completionRate(session.getCompletionRate())
                .currentStep(session.getCurrentStep())
                .startTime(session.getKitchenExitTime())
                .elapsedMinutes(elapsedMinutes)
                .build();
    }

    private SessionAwaitingResponse mapToAwaitingResponse(ServiceSession session) {
        long waitingMinutes = session.getNurseAlertTime() != null ?
                ChronoUnit.MINUTES.between(session.getNurseAlertTime(), LocalDateTime.now()) : 0;

        return SessionAwaitingResponse.builder()
                .sessionId(session.getId())
                .sessionIdString(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .wardName(session.getWard().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .nurseAlertTime(session.getNurseAlertTime())
                .waitingMinutes(waitingMinutes)
                .build();
    }

    private Double calculateAverageCompletionRate(LocalDateTime since) {
        // This would be implemented with a proper query
        return 85.5; // Placeholder
    }

    private Double calculateAverageServingTime(LocalDateTime since) {
        // This would be implemented with a proper query
        return 12.3; // Placeholder in minutes
    }
}