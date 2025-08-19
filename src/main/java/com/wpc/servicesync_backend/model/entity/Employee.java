// Employee.java - Fixed version with proper JSONB handling 👨‍⚕️👩‍⚕️

package com.wpc.servicesync_backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // 🆔 Unique identifier

    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    private String employeeId; // 🏷️ Employee badge number

    @Column(name = "name", nullable = false)
    private String name; // 👤 Full name

    @Column(name = "email", unique = true)
    private String email; // 📧 Contact email

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // 🔐 Secure password hash

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private EmployeeRole role; // 👔 Job position

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital; // 🏥 Workplace

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // ✅ Employment status

    @Column(name = "last_login")
    private LocalDateTime lastLogin; // ⏰ Last system access

    // OPTION 1: Use JdbcTypeCode for JSON handling (Recommended) 📅
    @Column(name = "shift_schedule", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String shiftSchedule; // 🗓️ Work schedule in JSON format

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 📅 Record creation time

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 🔄 Last update time

    // Business Methods 🏥

    /**
     * Get display name with role emoji for UI purposes 🏷️
     */
    public String getDisplayName() {
        String emoji = getRoleEmoji();
        return emoji + " " + name;
    }

    /**
     * Get emoji based on employee role 🎭
     */
    private String getRoleEmoji() {
        if (role == null) return "👤";

        switch (role) {
            case HOSTESS:
                return "👩‍⚕️";
            case NURSE:
                return "👨‍⚕️";
            case SUPERVISOR:
                return "👔";
            case ADMIN:
                return "🛡️";
            default:
                return "👤";
        }
    }

    /**
     * Check if employee can access a specific ward 🚪
     * Admin and Supervisor have full access
     * Nurses and Hostesses can only access wards in their shift schedule
     */
    public boolean canAccessWard(String wardName) {
        if (wardName == null || wardName.trim().isEmpty()) {
            return false;
        }

        // Role-based access control
        switch (role) {
            case ADMIN:
            case SUPERVISOR:
                return true; // 🔑 Full access

            case NURSE:
            case HOSTESS:
                return canAccessAssignedWard(wardName); // 📅 Schedule-based access

            default:
                return false;
        }
    }

    /**
     * Check if ward is in employee's assigned schedule 📋
     */
    private boolean canAccessAssignedWard(String wardName) {
        if (shiftSchedule == null || shiftSchedule.trim().isEmpty()) {
            return false;
        }

        // Handle both JSON format and simple comma-separated format
        String schedule = shiftSchedule.toLowerCase();
        String ward = wardName.toLowerCase();

        // If it's JSON format, extract ward names
        if (schedule.startsWith("{")) {
            return schedule.contains(ward);
        }

        // Simple comma-separated format: "3A,3B,4A"
        String[] assignedWards = schedule.split(",");
        for (String assignedWard : assignedWards) {
            if (assignedWard.trim().toLowerCase().equals(ward)) {
                return true;
            }
        }

        return false;
    }
}