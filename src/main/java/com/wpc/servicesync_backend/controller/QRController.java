package com.wpc.servicesync_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Tag(name = "QR Codes", description = "QR code generation and validation endpoints")
public class QRController {

    @PostMapping("/validate/{type}")
    @Operation(summary = "Validate QR code", description = "Validate scanned QR code")
    public ResponseEntity<Map<String, Object>> validateQRCode(
            @Parameter(description = "QR type") @PathVariable String type,
            @Parameter(description = "QR code content") @RequestBody String qrContent) {

        // Basic QR validation logic
        boolean isValid = qrContent != null && !qrContent.trim().isEmpty() &&
                qrContent.toUpperCase().startsWith(type.toUpperCase() + "_");

        Map<String, Object> response = Map.of(
                "valid", isValid,
                "type", type,
                "timestamp", LocalDateTime.now(),
                "message", isValid ? "QR code validated successfully" : "Invalid QR code"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate/{type}")
    @Operation(summary = "Generate QR code", description = "Generate QR code for testing purposes")
    public ResponseEntity<Map<String, String>> generateQRCode(
            @Parameter(description = "QR type") @PathVariable String type,
            @Parameter(description = "Location ID") @RequestParam(required = false) String locationId) {

        String qrContent = STR."\{type.toUpperCase()}_\{locationId != null ? locationId : UUID.randomUUID().toString().substring(0, 8)}|\{System.currentTimeMillis()}";

        Map<String, String> response = Map.of(
                "type", type,
                "content", qrContent,
                "locationId", locationId != null ? locationId : "AUTO_GENERATED"
        );

        return ResponseEntity.ok(response);
    }
}