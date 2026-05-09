package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserDegreeRepository extends JpaRepository<UserDegree, Integer> {

    List<UserDegree> findByUser_Id(UUID userId);

    Long countByDegree_Faculty_IdAndStatus(Integer facultyId, AcademicStatus status);
    // Contar expedientes académicos por carrera (para validar dependencias en delete)
    long countByDegree_Id(Integer degreeId);

    boolean existsByUser_IdAndDegree_IdAndStatus(UUID userId, Integer degreeId, AcademicStatus status);
}