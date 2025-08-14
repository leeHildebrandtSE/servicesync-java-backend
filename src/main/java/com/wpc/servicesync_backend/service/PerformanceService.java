package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.model.dto.PerformanceReportDto;
import com.wpc.servicesync_backend.model.dto.SessionSummaryDto;
import com.wpc.servicesync_backend.model.entity.ServiceSession;
import com.wpc.servicesync_backend.repository.ServiceSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PerformanceService {

    private final ServiceSessionRepository sessionRepository;

    @Cacheable(value = "performance-reports", key = "#date.toString() + '_daily'")
    public PerformanceReportDto generateDailyReport(LocalDateTime date) {
        log.info("Generating daily performance report for date: {}", date);

        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return generateReportForPeriod(startOfDay, endOfDay, "Daily");
    }

    @Cacheable(value = "performance-reports", key = "#weekStart.toString() + '_weekly'")
    public PerformanceReportDto generateWeeklyReport(LocalDateTime weekStart) {
        log.info("Generating weekly performance report for week starting: {}", weekStart);

        LocalDateTime startOfWeek = weekStart.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfWeek = startOfWeek.plusWeeks(1);

        return generateReportForPeriod(startOfWeek, endOfWeek, "Weekly");
    }

    @Cacheable(value = "performance-reports", key = "#monthStart.toString() + '_monthly'")
    public PerformanceReportDto generateMonthlyReport(LocalDateTime monthStart) {
        log.info("Generating monthly performance report for month starting: {}", monthStart);

        LocalDateTime startOfMonth = monthStart.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        return generateReportForPeriod(startOfMonth, endOfMonth, "Monthly");
    }

    @Cacheable(value = "performance-reports", key = "#hospitalId.toString() + '_' + #fromDate.toString() + '_hospital'")
    public PerformanceReportDto generateHospitalReport(UUID hospitalId, LocalDateTime fromDate) {
        log.info("Generating hospital performance report for hospital: {} from date: {}", hospitalId, fromDate);

        List<ServiceSession> sessions = sessionRepository.findRecentSessionsByHospital(hospitalId, fromDate);

        return buildPerformanceReport(sessions, "Hospital-specific", fromDate);
    }

    private PerformanceReportDto generateReportForPeriod(LocalDateTime start, LocalDateTime end, String period) {
        List<ServiceSession> sessions = sessionRepository.findCompletedSessionsBetween(start, end);
        return buildPerformanceReport(sessions, period, start);
    }

    private PerformanceReportDto buildPerformanceReport(List<ServiceSession> sessions, String period, LocalDateTime reportDate) {
        if (sessions.isEmpty()) {
            return PerformanceReportDto.builder()
                    .reportDate(reportDate)
                    .reportPeriod(period)
                    .totalSessions(0)
                    .completedSessions(0)
                    .averageCompletionRate(0.0)
                    .averageTravelTimeMinutes(0L)
                    .averageNurseResponseTimeMinutes(0L)
                    .averageServingTimeMinutes(0L)
                    .averageServingRate(0.0)
                    .efficiencyRating("No Data")
                    .topPerformingSessions(List.of())
                    .problematicSessions(List.of())
                    .build();
        }

        int totalSessions = sessions.size();
        int completedSessions = (int) sessions.stream()
                .filter(ServiceSession::isCompleted)
                .count();

        double averageCompletionRate = sessions.stream()
                .mapToDouble(ServiceSession::getCompletionRate)
                .average()
                .orElse(0.0);

        long averageTravelTime = (long) sessions.stream()
                .mapToLong(ServiceSession::getTravelTime)
                .filter(time -> time > 0)
                .average()
                .orElse(0.0) / 60000; // Convert to minutes

        long averageNurseResponseTime = (long) sessions.stream()
                .mapToLong(ServiceSession::getNurseResponseTime)
                .filter(time -> time > 0)
                .average()
                .orElse(0.0) / 60000; // Convert to minutes

        long averageServingTime = (long) sessions.stream()
                .mapToLong(ServiceSession::getServingTime)
                .filter(time -> time > 0)
                .average()
                .orElse(0.0) / 60000; // Convert to minutes

        double averageServingRate = sessions.stream()
                .mapToDouble(ServiceSession::getAverageServingRate)
                .filter(rate -> rate > 0)
                .average()
                .orElse(0.0);

        String efficiencyRating = calculateOverallEfficiency(averageCompletionRate, averageServingRate);

        List<SessionSummaryDto> topPerforming = sessions.stream()
                .filter(s -> s.getCompletionRate() >= 95.0 && s.getAverageServingRate() >= 0.8)
                .sorted((a, b) -> Double.compare(b.getAverageServingRate(), a.getAverageServingRate())) // Fixed: Use Double.compare
                .limit(5)
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());

        List<SessionSummaryDto> problematic = sessions.stream()
                .filter(s -> s.getCompletionRate() < 75.0 || s.getTravelTime() > 900000 || s.getNurseResponseTime() > 300000)
                .sorted((a, b) -> Double.compare(a.getCompletionRate(), b.getCompletionRate())) // Fixed: Use Double.compare
                .limit(5)
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());

        return PerformanceReportDto.builder()
                .reportDate(reportDate)
                .reportPeriod(period)
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .averageCompletionRate(averageCompletionRate)
                .averageTravelTimeMinutes(averageTravelTime)
                .averageNurseResponseTimeMinutes(averageNurseResponseTime)
                .averageServingTimeMinutes(averageServingTime)
                .averageServingRate(averageServingRate)
                .efficiencyRating(efficiencyRating)
                .topPerformingSessions(topPerforming)
                .problematicSessions(problematic)
                .build();
    }

    private String calculateOverallEfficiency(double completionRate, double servingRate) {
        if (completionRate >= 95.0 && servingRate >= 0.8) {
            return "Excellent";
        } else if (completionRate >= 85.0 && servingRate >= 0.6) {
            return "Good";
        } else if (completionRate >= 75.0 && servingRate >= 0.4) {
            return "Acceptable";
        } else {
            return "Below Average";
        }
    }

    private SessionSummaryDto convertToSummaryDto(ServiceSession session) {
        return SessionSummaryDto.builder()
                .sessionId(session.getSessionId())
                .employeeName(session.getEmployee().getName())
                .wardName(session.getWard().getName())
                .mealType(session.getMealType())
                .mealCount(session.getMealCount())
                .completionRate(session.getCompletionRate()) // Fixed: Removed unnecessary cast
                .totalDurationMinutes(session.getElapsedTime() / 60000)
                .efficiencyRating(session.getEfficiencyRating())
                .createdAt(session.getCreatedAt())
                .build();
    }
}