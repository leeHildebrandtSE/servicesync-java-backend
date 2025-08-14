// src/main/java/com/wpc/servicesync_backend/service/NotificationService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.dto.ServiceSessionResponse;
import com.wpc.servicesync_backend.model.entity.ServiceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("taskExecutor")
    public void sendSessionUpdate(ServiceSession session) {
        try {
            ServiceSessionResponse response = mapToResponse(session);

            // Send to all subscribers
            messagingTemplate.convertAndSend("/topic/sessions", response);

            // Send to specific employee
            messagingTemplate.convertAndSendToUser(
                    session.getEmployee().getEmployeeId(),
                    "/queue/sessions",
                    response
            );

            // Send to ward-specific topic
            messagingTemplate.convertAndSend(
                    "/topic/ward/" + session.getWard().getId(),
                    response
            );

            log.info("Session update notification sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session update notification", e);
        }
    }

    @Async("taskExecutor")
    public void sendNurseAlert(ServiceSession session) {
        try {
            Map<String, Object> alert = Map.of(
                    "type", "NURSE_ALERT",
                    "sessionId", session.getSessionId(),
                    "wardId", session.getWard().getId(),
                    "wardName", session.getWard().getName(),
                    "mealType", session.getMealType().getDisplayName(),
                    "mealCount", session.getMealCount(),
                    "employeeName", session.getEmployee().getName(),
                    "timestamp", LocalDateTime.now(),
                    "urgency", "HIGH"
            );

            // Send to nurse station for this ward
            messagingTemplate.convertAndSend("/topic/nurse-alerts/" + session.getWard().getId(), alert);

            // Send to all supervisors
            messagingTemplate.convertAndSend("/topic/supervisor-alerts", alert);

            log.info("Nurse alert sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send nurse alert", e);
        }
    }

    @Async("taskExecutor")
    public void sendNurseResponse(ServiceSession session, String nurseName) {
        try {
            Map<String, Object> response = Map.of(
                    "type", "NURSE_RESPONSE",
                    "sessionId", session.getSessionId(),
                    "wardId", session.getWard().getId(),
                    "nurseName", nurseName,
                    "responseTime", LocalDateTime.now(),
                    "status", "ACKNOWLEDGED"
            );

            // Notify the hostess
            messagingTemplate.convertAndSendToUser(
                    session.getEmployee().getEmployeeId(),
                    "/queue/nurse-responses",
                    response
            );

            log.info("Nurse response notification sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send nurse response notification", e);
        }
    }

    @Async("taskExecutor")
    public void sendPerformanceAlert(ServiceSession session, String alertType, String message) {
        try {
            Map<String, Object> alert = Map.of(
                    "type", "PERFORMANCE_ALERT",
                    "alertType", alertType,
                    "sessionId", session.getSessionId(),
                    "message", message,
                    "employeeName", session.getEmployee().getName(),
                    "wardName", session.getWard().getName(),
                    "timestamp", LocalDateTime.now(),
                    "urgency", determineUrgency(alertType)
            );

            // Send to supervisors
            messagingTemplate.convertAndSend("/topic/supervisor-alerts", alert);

            // Send to employee if it's their performance issue
            messagingTemplate.convertAndSendToUser(
                    session.getEmployee().getEmployeeId(),
                    "/queue/performance-alerts",
                    alert
            );

            log.info("Performance alert sent for session: {} - {}", session.getSessionId(), alertType);
        } catch (Exception e) {
            log.error("Failed to send performance alert", e);
        }
    }

    @Async("taskExecutor")
    public void sendSessionCompleted(ServiceSession session) {
        try {
            Map<String, Object> completion = Map.of(
                    "type", "SESSION_COMPLETED",
                    "sessionId", session.getSessionId(),
                    "completionRate", session.getCompletionRate(),
                    "totalDuration", session.getElapsedTime(),
                    "efficiencyRating", session.getEfficiencyRating(),
                    "mealsServed", session.getMealsServed(),
                    "mealCount", session.getMealCount(),
                    "timestamp", LocalDateTime.now()
            );

            // Notify all stakeholders
            messagingTemplate.convertAndSend("/topic/session-completions", completion);

            log.info("Session completion notification sent for: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session completion notification", e);
        }
    }

    private String determineUrgency(String alertType) {
        return switch (alertType.toLowerCase()) {
            case "travel_time_exceeded", "nurse_response_timeout" -> "HIGH";
            case "low_completion_rate", "slow_serving_rate" -> "MEDIUM";
            default -> "LOW";
        };
    }

    private ServiceSessionResponse mapToResponse(ServiceSession session) {
        return ServiceSessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .employeeId(session.getEmployee().getEmployeeId())
                .wardName(session.getWard().getName())
                .hospitalName(session.getWard().getHospital().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .mealsServed(session.getMealsServed())
                .status(session.getStatus())
                .completionRate(session.getCompletionRate())
                .currentStep(session.getCurrentStep())
                .efficiencyRating(session.getEfficiencyRating())
                .kitchenExitTime(session.getKitchenExitTime())
                .wardArrivalTime(session.getWardArrivalTime())
                .nurseAlertTime(session.getNurseAlertTime())
                .nurseResponseTime(session.getNurseResponseTime())
                .serviceStartTime(session.getServiceStartTime())
                .serviceCompleteTime(session.getServiceCompleteTime())
                .travelTimeSeconds(session.getTravelTime() / 1000)
                .nurseResponseTimeSeconds(session.getNurseResponseTime() / 1000)
                .servingTimeSeconds(session.getServingTime() / 1000)
                .totalDurationSeconds(session.getElapsedTime() / 1000)
                .comments(session.getComments())
                .nurseName(session.getNurseName())
                .dietSheetDocumented(session.getDietSheetDocumented())
                .dietSheetNotes(session.getDietSheetNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}