package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDegreeRepository extends JpaRepository<UserDegree, Integer> {
    // Busca si ya existe un expediente para ese usuario en esa carrera
    boolean existsByUser_IdAndDegree_Id(UUID userId, Integer degreeId);

    // Trae todos los expedientes de un usuario
    List<UserDegree> findByUser_Id(UUID userId);

    long countByDegree_FacultyIdAndStatus(Integer facultyId, AcademicStatus status);
}
