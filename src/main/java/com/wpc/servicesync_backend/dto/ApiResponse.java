// src/main/java/com/wpc/servicesync_backend/dto/ApiResponse.java
package com.wpc.servicesync_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified API Response wrapper for all ServiceSync endpoints
 * Provides consistent response format across the application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private Object metadata;

    // Factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, Object metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory methods for error responses
    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .message("Operation failed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Fixed: Removed duplicate method signature
    public static <T> ApiResponse<T> errorWithPath(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> errorWithData(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory method for validation errors
    public static <T> ApiResponse<T> validationError(String message, T validationDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(validationDetails)
                .error("Validation failed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory method for paginated responses
    public static <T> ApiResponse<T> paginated(T data, PaginationMetadata pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Data retrieved successfully")
                .data(data)
                .metadata(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Nested class for pagination metadata
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}