// src/main/java/com/wpc/servicesync_backend/dto/QRScanRequest.java
package com.wpc.servicesync_backend.dto;

import com.wpc.servicesync_backend.model.entity.QRLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class QRScanRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotBlank(message = "QR code content is required")
    private String qrCodeContent;

    @NotNull(message = "QR location type is required")
    private QRLocationType locationType;

    private String additionalData;
}