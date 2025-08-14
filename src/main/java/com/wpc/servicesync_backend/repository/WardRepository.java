// src/main/java/com/wpc/servicesync_backend/repository/WardRepository.java
package com.wpc.servicesync_backend.repository;

import com.wpc.servicesync_backend.model.entity.Ward;
import com.wpc.servicesync_backend.model.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID> {

    List<Ward> findByHospitalAndIsActiveTrue(Hospital hospital);

    List<Ward> findByHospital_IdAndIsActiveTrue(UUID hospitalId);

    Optional<Ward> findByNameAndHospital(String name, Hospital hospital);

    @Query("SELECT w FROM Ward w WHERE w.hospital.id = :hospitalId AND w.isActive = true ORDER BY w.name")
    List<Ward> findActiveWardsByHospitalOrderByName(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT w FROM Ward w WHERE w.isActive = true ORDER BY w.hospital.name, w.name")
    List<Ward> findAllActiveWardsOrderByHospitalAndName();

    boolean existsByNameAndHospital(String name, Hospital hospital);

    @Query("SELECT COUNT(w) FROM Ward w WHERE w.hospital.id = :hospitalId AND w.isActive = true")
    long countActiveWardsByHospital(@Param("hospitalId") UUID hospitalId);
}