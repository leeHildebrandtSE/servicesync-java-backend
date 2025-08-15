package com.wpc.servicesync_backend.validation;

import com.wpc.servicesync_backend.dto.ServiceSessionRequest;
import com.wpc.servicesync_backend.model.entity.MealType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

public class ValidServiceSessionValidator implements ConstraintValidator<ValidServiceSession, ServiceSessionRequest> {

    @Override
    public boolean isValid(ServiceSessionRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // Validate meal count based on meal type
        if (request.getMealType() != null && request.getMealCount() != null) {
            int maxCount = getMaxMealCountForType(request.getMealType());
            if (request.getMealCount() > maxCount) {
                context.buildConstraintViolationWithTemplate(
                                String.format("Meal count for %s cannot exceed %d",
                                        request.getMealType().getDisplayName(), maxCount))
                        .addPropertyNode("mealCount")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Validate meal type timing (optional business rule)
        if (request.getMealType() != null && !isMealTypeValidForCurrentTime(request.getMealType())) {
            context.buildConstraintViolationWithTemplate(
                            String.format("Current time is outside normal serving hours for %s",
                                    request.getMealType().getDisplayName()))
                    .addPropertyNode("mealType")
                    .addConstraintViolation();
            // Note: This is a warning, not a blocking validation
        }

        return isValid;
    }

    private int getMaxMealCountForType(MealType mealType) {
        return switch (mealType) {
            case BREAKFAST, LUNCH, SUPPER -> 50;
            case BEVERAGES -> 100;
        };
    }

    private boolean isMealTypeValidForCurrentTime(MealType mealType) {
        LocalTime now = LocalTime.now();
        return switch (mealType) {
            case BREAKFAST -> now.isAfter(LocalTime.of(5, 30)) && now.isBefore(LocalTime.of(10, 0));
            case LUNCH -> now.isAfter(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(15, 0));
            case SUPPER -> now.isAfter(LocalTime.of(16, 30)) && now.isBefore(LocalTime.of(20, 30));
            case BEVERAGES -> true; // Available all day
        };
    }
}