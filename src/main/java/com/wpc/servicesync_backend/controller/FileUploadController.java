// File: src/main/java/com/wpc/servicesync_backend/controller/FileUploadController.java
package com.wpc.servicesync_backend.controller;

import com.wpc.servicesync_backend.dto.ApiResponse;
import com.wpc.servicesync_backend.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "File upload and management endpoints")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/diet-sheet/{sessionId}")
    @Operation(summary = "Upload diet sheet photo", description = "Upload diet sheet photo for a session")
    @PreAuthorize("hasRole('HOSTESS') or hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadDietSheetPhoto(
            @Parameter(description = "Session ID") @PathVariable String sessionId,
            @Parameter(description = "Diet sheet image file") @RequestParam("file") MultipartFile file) {
        log.info("Uploading diet sheet photo for session: {}", sessionId);

        try {
            Map<String, Object> result = fileUploadService.uploadDietSheetPhoto(file, sessionId);
            return ResponseEntity.ok(ApiResponse.success("Diet sheet photo uploaded successfully", result));
        } catch (Exception e) {
            log.error("Error uploading diet sheet photo for session: {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload diet sheet photo", e.getMessage()));
        }
    }

    @PostMapping("/profile/{employeeId}")
    @Operation(summary = "Upload profile photo", description = "Upload profile photo for an employee")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadProfilePhoto(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Profile image file") @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile photo for employee: {}", employeeId);

        try {
            Map<String, Object> result = fileUploadService.uploadProfilePhoto(file, employeeId);
            return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", result));
        } catch (Exception e) {
            log.error("Error uploading profile photo for employee: {}", employeeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload profile photo", e.getMessage()));
        }
    }

    @GetMapping("/{fileType}/{fileName}")
    @Operation(summary = "Get file", description = "Download file by name and type")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "File type (diet-sheet, profile)") @PathVariable String fileType,
            @Parameter(description = "File name") @PathVariable String fileName) {
        log.info("Retrieving file: {} of type: {}", fileName, fileType);

        try {
            byte[] fileData = fileUploadService.getFile(fileName, fileType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(fileData);
        } catch (Exception e) {
            log.error("Error retrieving file: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileType}/{fileName}")
    @Operation(summary = "Delete file", description = "Delete file by name and type")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @Parameter(description = "File type") @PathVariable String fileType,
            @Parameter(description = "File name") @PathVariable String fileName) {
        log.info("Deleting file: {} of type: {}", fileName, fileType);

        try {
            boolean deleted = fileUploadService.deleteFile(fileName, fileType);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileName, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete file", e.getMessage()));
        }
    }
}