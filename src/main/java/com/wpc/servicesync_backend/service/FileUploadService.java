// src/main/java/com/wpc/servicesync_backend/service/FileUploadService.java
package com.wpc.servicesync_backend.service;

import com.wpc.servicesync_backend.config.ApplicationProperties;
import com.wpc.servicesync_backend.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final ApplicationProperties applicationProperties;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public Map<String, Object> uploadDietSheetPhoto(MultipartFile file, String sessionId) {
        log.info("Uploading diet sheet photo for session: {}", sessionId);

        validateFile(file);

        try {
            String fileName = generateFileName(file, sessionId);
            String filePath = saveDietSheetFile(file, fileName);

            log.info("Diet sheet photo uploaded successfully: {}", filePath);

            return Map.of(
                    "success", true,
                    "fileName", fileName,
                    "filePath", filePath,
                    "fileSize", file.getSize(),
                    "contentType", file.getContentType(),
                    "uploadTime", LocalDateTime.now()
            );
        } catch (IOException e) {
            log.error("Failed to upload diet sheet photo for session: {}", sessionId, e);
            throw ServiceException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadProfilePhoto(MultipartFile file, String employeeId) {
        log.info("Uploading profile photo for employee: {}", employeeId);

        validateFile(file);

        try {
            String fileName = generateProfileFileName(file, employeeId);
            String filePath = saveProfileFile(file, fileName);

            log.info("Profile photo uploaded successfully: {}", filePath);

            return Map.of(
                    "success", true,
                    "fileName", fileName,
                    "filePath", filePath,
                    "fileSize", file.getSize(),
                    "contentType", file.getContentType(),
                    "uploadTime", LocalDateTime.now()
            );
        } catch (IOException e) {
            log.error("Failed to upload profile photo for employee: {}", employeeId, e);
            throw ServiceException.internalError("Failed to upload file: " + e.getMessage());
        }
    }

    public byte[] getFile(String fileName, String fileType) {
        try {
            Path filePath = switch (fileType.toLowerCase()) {
                case "diet-sheet" -> Paths.get(applicationProperties.getStorage().getLocal().getDietSheetDir(), fileName);
                case "profile" -> Paths.get(applicationProperties.getStorage().getLocal().getUploadDir(), "profiles", fileName);
                default -> throw ServiceException.badRequest("Invalid file type: " + fileType);
            };

            if (!Files.exists(filePath)) {
                throw ServiceException.notFound("File not found: " + fileName);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read file: {}", fileName, e);
            throw ServiceException.internalError("Failed to read file: " + e.getMessage());
        }
    }

    public boolean deleteFile(String fileName, String fileType) {
        try {
            Path filePath = switch (fileType.toLowerCase()) {
                case "diet-sheet" -> Paths.get(applicationProperties.getStorage().getLocal().getDietSheetDir(), fileName);
                case "profile" -> Paths.get(applicationProperties.getStorage().getLocal().getUploadDir(), "profiles", fileName);
                default -> throw ServiceException.badRequest("Invalid file type: " + fileType);
            };

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileName);
                return true;
            }

            log.warn("File not found for deletion: {}", fileName);
            return false;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
            throw ServiceException.internalError("Failed to delete file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceException.badRequest("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw ServiceException.badRequest("File size exceeds maximum allowed size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw ServiceException.badRequest("Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed");
        }
    }

    private String generateFileName(MultipartFile file, String sessionId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(file.getOriginalFilename());
        return String.format("diet_sheet_%s_%s_%s.%s", sessionId, timestamp, UUID.randomUUID().toString().substring(0, 8), extension);
    }

    private String generateProfileFileName(MultipartFile file, String employeeId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(file.getOriginalFilename());
        return String.format("profile_%s_%s.%s", employeeId, timestamp, extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg"; // Default extension
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String saveDietSheetFile(MultipartFile file, String fileName) throws IOException {
        Path uploadDir = Paths.get(applicationProperties.getStorage().getLocal().getDietSheetDir());
        createDirectoryIfNotExists(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private String saveProfileFile(MultipartFile file, String fileName) throws IOException {
        Path uploadDir = Paths.get(applicationProperties.getStorage().getLocal().getUploadDir(), "profiles");
        createDirectoryIfNotExists(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private void createDirectoryIfNotExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created upload directory: {}", directory);
        }
    }
}