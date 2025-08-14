// src/main/java/com/wpc/servicesync_backend/repository/ServiceSessionRepository.java
package com.wpc.servicesync_backend.repository;

import com.wpc.servicesync_backend.model.entity.ServiceSession;
import com.wpc.servicesync_backend.model.entity.Employee;
import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.model.entity.SessionStatus;
import com.wpc.servicesync_backend.model.entity.MealType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceSessionRepository extends JpaRepository<ServiceSession, UUID> {

    Optional<ServiceSession> findBySessionId(String sessionId);

    List<ServiceSession> findByEmployeeAndStatusOrderByCreatedAtDesc(Employee employee, SessionStatus status);

    List<ServiceSession> findByWardAndStatusOrderByCreatedAtDesc(Ward ward, SessionStatus status);

    @Query("SELECT s FROM ServiceSession s WHERE s.employee.id = :employeeId AND s.status = :status ORDER BY s.createdAt DESC")
    List<ServiceSession> findByEmployeeIdAndStatus(@Param("employeeId") UUID employeeId, @Param("status") SessionStatus status);

    @Query("SELECT s FROM ServiceSession s WHERE s.ward.id = :wardId AND s.status = :status ORDER BY s.createdAt DESC")
    List<ServiceSession> findByWardIdAndStatus(@Param("wardId") UUID wardId, @Param("status") SessionStatus status);

    @Query("SELECT s FROM ServiceSession s WHERE s.employee.id = :employeeId AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<ServiceSession> findRecentSessionsByEmployee(@Param("employeeId") UUID employeeId, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM ServiceSession s WHERE s.ward.hospital.id = :hospitalId AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<ServiceSession> findRecentSessionsByHospital(@Param("hospitalId") UUID hospitalId, @Param("since") LocalDateTime since);

    Page<ServiceSession> findByStatusOrderByCreatedAtDesc(SessionStatus status, Pageable pageable);

    @Query("SELECT s FROM ServiceSession s WHERE s.status = :status AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<ServiceSession> findActiveSessionsSince(@Param("status") SessionStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM ServiceSession s WHERE s.mealType = :mealType AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<ServiceSession> findSessionsByMealTypeAndDate(@Param("mealType") MealType mealType, @Param("since") LocalDateTime since);

    // Performance analytics queries
    @Query("SELECT AVG(s.mealsServed) FROM ServiceSession s WHERE s.status = 'COMPLETED' AND s.createdAt >= :since")
    Double getAverageMealsServedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM ServiceSession s WHERE s.status = 'COMPLETED' AND s.createdAt >= :since")
    long getCompletedSessionsCountSince(@Param("since") LocalDateTime since);

    @Query("SELECT s FROM ServiceSession s WHERE s.employee.id = :employeeId AND s.status = 'COMPLETED' ORDER BY s.createdAt DESC")
    Page<ServiceSession> findCompletedSessionsByEmployee(@Param("employeeId") UUID employeeId, Pageable pageable);

    @Query("SELECT s FROM ServiceSession s WHERE s.ward.id = :wardId AND s.status = 'COMPLETED' ORDER BY s.createdAt DESC")
    Page<ServiceSession> findCompletedSessionsByWard(@Param("wardId") UUID wardId, Pageable pageable);

    // Real-time monitoring queries
    @Query("SELECT s FROM ServiceSession s WHERE s.status IN ('ACTIVE', 'IN_TRANSIT') ORDER BY s.createdAt DESC")
    List<ServiceSession> findAllActiveSessions();

    @Query("SELECT s FROM ServiceSession s WHERE s.status = 'ACTIVE' AND s.kitchenExitTime IS NOT NULL AND s.serviceCompleteTime IS NULL")
    List<ServiceSession> findSessionsInProgress();

    @Query("SELECT s FROM ServiceSession s WHERE s.status = 'ACTIVE' AND s.nurseAlertTime IS NOT NULL AND s.nurseResponseTime IS NULL")
    List<ServiceSession> findSessionsAwaitingNurseResponse();

    // Statistics queries
    @Query("SELECT s.mealType, COUNT(s) FROM ServiceSession s WHERE s.createdAt >= :since GROUP BY s.mealType")
    List<Object[]> getMealTypeStatisticsSince(@Param("since") LocalDateTime since);

    @Query("SELECT s.ward.name, COUNT(s) FROM ServiceSession s WHERE s.createdAt >= :since GROUP BY s.ward.name ORDER BY COUNT(s) DESC")
    List<Object[]> getWardActivityStatisticsSince(@Param("since") LocalDateTime since);

    @Query("SELECT s.employee.name, COUNT(s) FROM ServiceSession s WHERE s.createdAt >= :since GROUP BY s.employee.name ORDER BY COUNT(s) DESC")
    List<Object[]> getEmployeeActivityStatisticsSince(@Param("since") LocalDateTime since);

    @Query("SELECT s FROM ServiceSession s WHERE s.createdAt BETWEEN :start AND :end AND s.status = 'COMPLETED' ORDER BY s.createdAt DESC")
    List<ServiceSession> findCompletedSessionsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM ServiceSession s WHERE s.status = 'ACTIVE' AND s.createdAt < :cutoff")
    List<ServiceSession> findStaleActiveSessions(@Param("cutoff") LocalDateTime cutoff);
}