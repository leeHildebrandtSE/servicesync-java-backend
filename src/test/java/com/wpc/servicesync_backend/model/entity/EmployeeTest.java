package com.wpc.servicesync_backend.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospital = Hospital.builder()
                .id(UUID.randomUUID())
                .code("TEST_HOSPITAL")
                .name("Test Hospital")
                .build();

        employee = Employee.builder()
                .id(UUID.randomUUID())
                .employeeId("H001")
                .name("Test Employee")
                .email("test@example.com")
                .role(EmployeeRole.HOSTESS)
                .hospital(hospital)
                .shiftSchedule("3A,3B,4A")
                .isActive(true)
                .build();
    }

    @Test
    void getDisplayName_Hostess() {
        // Given
        employee.setRole(EmployeeRole.HOSTESS);

        // When
        String displayName = employee.getDisplayName();

        // Then
        assertEquals("👩‍⚕️ Test Employee", displayName);
    }

    @Test
    void getDisplayName_Nurse() {
        // Given
        employee.setRole(EmployeeRole.NURSE);

        // When
        String displayName = employee.getDisplayName();

        // Then
        assertEquals("👨‍⚕️ Test Employee", displayName);
    }

    @Test
    void getDisplayName_Supervisor() {
        // Given
        employee.setRole(EmployeeRole.SUPERVISOR);

        // When
        String displayName = employee.getDisplayName();

        // Then
        assertEquals("👔 Test Employee", displayName);
    }

    @Test
    void getDisplayName_Admin() {
        // Given
        employee.setRole(EmployeeRole.ADMIN);

        // When
        String displayName = employee.getDisplayName();

        // Then
        assertEquals("🛡️ Test Employee", displayName);
    }

    @Test
    void canAccessWard_AdminCanAccessAll() {
        // Given
        employee.setRole(EmployeeRole.ADMIN);

        // When & Then
        assertTrue(employee.canAccessWard("3A"));
        assertTrue(employee.canAccessWard("5B"));
        assertTrue(employee.canAccessWard("ANY_WARD"));
    }

    @Test
    void canAccessWard_SupervisorCanAccessAll() {
        // Given
        employee.setRole(EmployeeRole.SUPERVISOR);

        // When & Then
        assertTrue(employee.canAccessWard("3A"));
        assertTrue(employee.canAccessWard("5B"));
        assertTrue(employee.canAccessWard("ANY_WARD"));
    }

    @Test
    void canAccessWard_HostessCanAccessAssignedWards() {
        // Given
        employee.setRole(EmployeeRole.HOSTESS);
        employee.setShiftSchedule("3A,3B,4A");

        // When & Then
        assertTrue(employee.canAccessWard("3A"));
        assertTrue(employee.canAccessWard("3B"));
        assertTrue(employee.canAccessWard("4A"));
        assertFalse(employee.canAccessWard("5B"));
    }

    @Test
    void canAccessWard_NurseCanAccessAssignedWards() {
        // Given
        employee.setRole(EmployeeRole.NURSE);
        employee.setShiftSchedule("3A");

        // When & Then
        assertTrue(employee.canAccessWard("3A"));
        assertFalse(employee.canAccessWard("3B"));
    }

    @Test
    void canAccessWard_NoScheduleAssigned() {
        // Given
        employee.setRole(EmployeeRole.HOSTESS);
        employee.setShiftSchedule(null);

        // When & Then
        assertFalse(employee.canAccessWard("3A"));
    }

    @Test
    void canAccessWard_EmptySchedule() {
        // Given
        employee.setRole(EmployeeRole.NURSE);
        employee.setShiftSchedule("");

        // When & Then
        assertFalse(employee.canAccessWard("3A"));
    }
}