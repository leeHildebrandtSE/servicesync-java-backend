package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.SessionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SessionUpdateRequest {
    private UUID sessionId;

    @Size(max = 500, message = "Diet sheet photo path cannot exceed 500 characters")
    private String dietSheetPhotoPath;

    private SessionStatus status;

    // Timestamp fields with validation
    private LocalDateTime kitchenExitTime;
    private LocalDateTime wardArrivalTime;
    private LocalDateTime nurseAlertTime;
    private LocalDateTime nurseResponseTime;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceCompleteTime;

    @Min(value = 0, message = "Meals served cannot be negative")
    @Max(value = 100, message = "Meals served cannot exceed 100")
    private Integer mealsServed;

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;

    @Size(max = 255, message = "Nurse name cannot exceed 255 characters")
    private String nurseName;

    private Boolean dietSheetDocumented;

    @Size(max = 1000, message = "Diet sheet notes cannot exceed 1000 characters")
    private String dietSheetNotes;
}