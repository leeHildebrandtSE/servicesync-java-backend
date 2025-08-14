package com.wpc.servicesync_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidEmployeeIdValidator implements ConstraintValidator<ValidEmployeeId, String> {

    @Override
    public void initialize(ValidEmployeeId constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String employeeId, ConstraintValidatorContext context) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }

        // Employee ID should be in format: [Letter][Number][Number][Number]
        // Examples: H001, N001, S001, A001
        return employeeId.matches("^[A-Z]\\d{3}$");
    }
}