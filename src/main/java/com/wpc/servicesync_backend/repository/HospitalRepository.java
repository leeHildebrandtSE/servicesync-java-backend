// src/main/java/com/wpc/servicesync_backend/repository/HospitalRepository.java
package com.wpc.servicesync_backend.repository;

import com.wpc.servicesync_backend.model.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    Optional<Hospital> findByCode(String code);

    List<Hospital> findByIsActiveTrue();

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.name")
    List<Hospital> findActiveHospitalsOrderByName();

    boolean existsByCode(String code);

    @Query("SELECT COUNT(h) FROM Hospital h WHERE h.isActive = true")
    long countActiveHospitals();
}