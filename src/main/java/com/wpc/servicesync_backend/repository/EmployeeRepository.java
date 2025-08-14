// src/main/java/com/wpc/servicesync_backend/repository/EmployeeRepository.java
package com.wpc.servicesync_backend.repository;

import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.EmployeeRole;
import com.wpc.servicesync_backend.model.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByHospitalAndIsActiveTrue(Hospital hospital);

    List<Employee> findByRoleAndIsActiveTrue(EmployeeRole role);

    @Query("SELECT e FROM Employee e WHERE e.hospital.id = :hospitalId AND e.role = :role AND e.isActive = true")
    List<Employee> findByHospitalAndRole(@Param("hospitalId") UUID hospitalId, @Param("role") EmployeeRole role);

    @Query("SELECT e FROM Employee e WHERE e.isActive = true ORDER BY e.hospital.name, e.role, e.name")
    List<Employee> findAllActiveEmployeesOrderByHospitalAndRole();

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.lastLogin >= :since")
    List<Employee> findEmployeesLoggedInSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.hospital.id = :hospitalId AND e.role = :role AND e.isActive = true")
    long countActiveEmployeesByHospitalAndRole(@Param("hospitalId") UUID hospitalId, @Param("role") EmployeeRole role);
}