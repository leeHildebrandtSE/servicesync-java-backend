package com.wpc.servicesync_backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees",
        indexes = {
                @Index(name = "idx_employee_id", columnList = "employee_id"),
                @Index(name = "idx_hospital_id", columnList = "hospital_id"),
                @Index(name = "idx_email", columnList = "email")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Employee ID is required")
    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Column(unique = true, length = 255)
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull(message = "Role is required")
    private EmployeeRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @NotNull(message = "Hospital is required")
    private Hospital hospital;

    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Password is required")
    private String passwordHash;

    @Column(name = "shift_schedule", columnDefinition = "jsonb")
    private String shiftSchedule;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Java 21 Pattern matching in business methods
    public String getDisplayName() {
        return switch (role) {
            case HOSTESS -> STR."👩‍⚕️ \{name}";
            case NURSE -> STR."👨‍⚕️ \{name}";
            case SUPERVISOR -> STR."👔 \{name}";
            case ADMIN -> STR."🛡️ \{name}";
        };
    }

    public boolean canAccessWard(String wardId) {
        return switch (role) {
            case ADMIN, SUPERVISOR -> true;
            case HOSTESS, NURSE -> {
                // Check if assigned to this ward
                yield shiftSchedule != null && shiftSchedule.contains(wardId);
            }
        };
    }
}