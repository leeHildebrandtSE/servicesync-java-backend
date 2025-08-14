package com.wpc.servicesync_backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "service_sessions",
        indexes = {
                @Index(name = "idx_session_id", columnList = "session_id"),
                @Index(name = "idx_employee_ward", columnList = "employee_id, ward_id"),
                @Index(name = "idx_status_created", columnList = "status, created_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ServiceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    @NotNull(message = "Ward is required")
    private Ward ward;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @Column(name = "meal_count", nullable = false)
    @Min(value = 1, message = "Meal count must be at least 1")
    @Max(value = 100, message = "Meal count cannot exceed 100")
    private Integer mealCount;

    @Column(name = "meals_served", nullable = false)
    @Builder.Default
    @Min(value = 0, message = "Meals served cannot be negative")
    private Integer mealsServed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    // Timestamp fields
    @Column(name = "kitchen_exit_time")
    private LocalDateTime kitchenExitTime;

    @Column(name = "ward_arrival_time")
    private LocalDateTime wardArrivalTime;

    @Column(name = "nurse_alert_time")
    private LocalDateTime nurseAlertTime;

    @Column(name = "nurse_response_time")
    private LocalDateTime nurseResponseTime;

    @Column(name = "service_start_time")
    private LocalDateTime serviceStartTime;

    @Column(name = "service_complete_time")
    private LocalDateTime serviceCompleteTime;

    // Additional fields
    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "nurse_name", length = 255)
    private String nurseName;

    // Diet sheet documentation
    @Column(name = "diet_sheet_photo_path")
    private String dietSheetPhotoPath;

    @Column(name = "diet_sheet_notes", columnDefinition = "TEXT")
    private String dietSheetNotes;

    @Column(name = "diet_sheet_documented", nullable = false)
    @Builder.Default
    private Boolean dietSheetDocumented = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods using Java 21 features
    public long getTravelTime() {
        if (kitchenExitTime != null && wardArrivalTime != null) {
            return ChronoUnit.MILLIS.between(kitchenExitTime, wardArrivalTime);
        }
        return 0L;
    }

    public long getNurseResponseTime() {
        if (nurseAlertTime != null && nurseResponseTime != null) {
            return ChronoUnit.MILLIS.between(nurseAlertTime, nurseResponseTime);
        }
        return 0L;
    }

    public long getServingTime() {
        if (serviceStartTime != null && serviceCompleteTime != null) {
            return ChronoUnit.MILLIS.between(serviceStartTime, serviceCompleteTime);
        }
        return 0L;
    }

    public long getElapsedTime() {
        if (kitchenExitTime != null && serviceCompleteTime != null) {
            return ChronoUnit.MILLIS.between(kitchenExitTime, serviceCompleteTime);
        } else if (kitchenExitTime != null) {
            return ChronoUnit.MILLIS.between(kitchenExitTime, LocalDateTime.now());
        }
        return 0L;
    }

    public double getCompletionRate() {
        if (mealCount == null || mealCount == 0) {
            return 0.0;
        }
        return (double) mealsServed / mealCount * 100.0;
    }

    public double getAverageServingRate() {
        long servingMillis = getServingTime();
        if (servingMillis > 0 && mealsServed > 0) {
            double minutes = servingMillis / 60000.0;
            return mealsServed / minutes;
        }
        return 0.0;
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    public boolean isOnSchedule() {
        return getAverageServingRate() >= 0.6; // 0.6 meals per minute threshold
    }

    public String getEfficiencyRating() {
        double completionRate = getCompletionRate();
        double servingRate = getAverageServingRate();

        // Fixed: Removed guard condition that's not allowed
        int ratingLevel = (int) completionRate / 25;
        return switch (ratingLevel) {
            case 4 -> servingRate >= 0.8 ? "Excellent" : "Good";
            case 3 -> servingRate >= 0.6 ? "Good" : "Acceptable";
            case 2 -> servingRate >= 0.4 ? "Acceptable" : "Below Average";
            default -> "Below Average";
        };
    }

    public String getCurrentStep() {
        return switch (status) {
            case ACTIVE -> {
                if (kitchenExitTime == null) yield "Kitchen Exit";
                if (wardArrivalTime == null) yield "Ward Arrival";
                if (!dietSheetDocumented) yield "Diet Sheet Documentation";
                if (nurseAlertTime == null) yield "Nurse Alert";
                if (nurseResponseTime == null) yield "Awaiting Nurse Response";
                if (serviceStartTime == null) yield "Nurse Station";
                yield "Service in Progress";
            }
            case COMPLETED -> "Service Complete";
            case CANCELLED -> "Service Cancelled";
            case IN_TRANSIT -> "In Transit";
        };
    }

    public String getSummary() {
        long elapsedMillis = getElapsedTime();
        double completionRate = getCompletionRate();
        String docStatus = dietSheetDocumented ? "✓" : "✗";

        return String.format("Ward %s • %d/%d %s meals • %.1f%% complete • Duration: %s • Doc: %s",
                ward != null ? ward.getName() : "Unknown",
                mealsServed, mealCount,
                mealType != null ? mealType.getDisplayName() : "Unknown",
                completionRate,
                formatDuration(elapsedMillis),
                docStatus);
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    // Additional utility methods
    public boolean hasWarnings() {
        long travelTime = getTravelTime();
        long nurseResponseTime = getNurseResponseTime();

        return travelTime > 900000L || // > 15 minutes travel
                nurseResponseTime > 300000L || // > 5 minutes nurse response
                getCompletionRate() < 75.0; // < 75% completion rate
    }

    public boolean isDocumentationComplete() {
        return dietSheetDocumented || (dietSheetPhotoPath != null && !dietSheetPhotoPath.trim().isEmpty());
    }
}