package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;

@Repository
public interface UserDegreeRepository extends JpaRepository<UserDegree, Integer> {
    
    boolean existsByUser_IdAndDegree_Id(UUID userId, Integer degreeId);

    List<UserDegree> findByUser_Id(UUID userId);

    // Misión US-12: Conteo para estadísticas administrativas
    Long countByDegree_Faculty_IdAndStatus(Integer facultyId, AcademicStatus status);
}