package com.wpc.servicesync_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidEmployeeIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmployeeId {
    String message() default "Invalid employee ID format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}