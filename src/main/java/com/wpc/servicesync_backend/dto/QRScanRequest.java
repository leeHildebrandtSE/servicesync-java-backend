package com.wpc.servicesync_backend.dto;

import com.wpc.servicesync_backend.model.entity.QRLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class QRScanRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotBlank(message = "QR code content is required")
    @Size(min = 5, max = 200, message = "QR code content must be between 5 and 200 characters")
    @Pattern(regexp = "^[A-Z_]+.*", message = "QR code must start with valid location prefix")
    private String qrCodeContent;

    @NotNull(message = "QR location type is required")
    private QRLocationType locationType;

    @Size(max = 255, message = "Additional data cannot exceed 255 characters")
    private String additionalData;
}