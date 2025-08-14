package com.wpc.servicesync_backend.model.dto;

import com.wpc.servicesync_backend.model.entity.MealType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class SessionCreateRequest {
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Ward ID is required")
    private UUID wardId;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @Min(value = 1, message = "Meal count must be at least 1")
    @Max(value = 100, message = "Meal count cannot exceed 100")
    private Integer mealCount;

    private String comments;
}