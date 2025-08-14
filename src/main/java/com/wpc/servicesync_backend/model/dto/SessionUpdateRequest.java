// src/main/java/com/wpc/servicesync_backend/dto/SessionUpdateRequest.java
package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SessionUpdateRequest {
    private UUID sessionId;
    private String dietSheetPhotoPath;
    private SessionStatus status;
    private LocalDateTime kitchenExitTime;
    private LocalDateTime wardArrivalTime;
    private LocalDateTime nurseAlertTime;
    private LocalDateTime nurseResponseTime;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceCompleteTime;
    private Integer mealsServed;
    private String comments;
    private String nurseName;
    private Boolean dietSheetDocumented;
    private String dietSheetNotes;
}