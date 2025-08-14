// Complete ServiceSession.java
package com.wpc.servicesync_backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.Duration;
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
@Builder
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
    public Duration getTravelTime() {
        if (kitchenExitTime != null && wardArrivalTime != null) {
            return Duration.between(kitchenExitTime, wardArrivalTime);
        }
        return Duration.ZERO;
    }

    public Duration getNurseResponseTime() {
        if (nurseAlertTime != null && nurseResponseTime != null) {
            return Duration.between(nurseAlertTime, nurseResponseTime);
        }
        return Duration.ZERO;
    }

    public Duration getServingTime() {
        if (serviceStartTime != null && serviceCompleteTime != null) {
            return Duration.between(serviceStartTime, serviceCompleteTime);
        }
        return Duration.ZERO;
    }

    public Duration getTotalDuration() {
        if (kitchenExitTime != null && serviceCompleteTime != null) {
            return Duration.between(kitchenExitTime, serviceCompleteTime);
        } else if (kitchenExitTime != null) {
            return Duration.between(kitchenExitTime, LocalDateTime.now());
        }
        return Duration.ZERO;
    }

    public double getCompletionRate() {
        if (mealCount == null || mealCount == 0) {
            return 0.0;
        }
        return (double) mealsServed / mealCount * 100.0;
    }

    public double getAverageServingRate() {
        Duration servingDuration = getServingTime();
        if (!servingDuration.isZero() && mealsServed > 0) {
            double minutes = servingDuration.toMinutes();
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

        return switch ((int) completionRate / 25) {
            case 4 when servingRate >= 0.8 -> "Excellent";
            case 3, 4 when servingRate >= 0.6 -> "Good";
            case 2, 3 when servingRate >= 0.4 -> "Acceptable";
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
        var duration = getTotalDuration();
        var completionRate = getCompletionRate();
        var docStatus = dietSheetDocumented ? "✓" : "✗";

        return STR."Ward \{ward.getName()} • \{mealsServed}/\{mealCount} \{mealType.getDisplayName()} meals • \{String.format("%.1f", completionRate)}% complete • Duration: \{formatDuration(duration)} • Doc: \{docStatus}";
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return STR."\{hours}h \{minutes}m \{seconds}s";
        } else if (minutes > 0) {
            return STR."\{minutes}m \{seconds}s";
        } else {
            return STR."\{seconds}s";
        }
    }
}