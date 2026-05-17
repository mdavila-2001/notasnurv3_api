package com.universidad_nur.notasnurv3_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.dto.dashboard.ManagementSummaryDTO;
import com.universidad_nur.notasnurv3_api.entities.Management;

@Repository
public interface ManagementRepository extends JpaRepository<Management, Integer> {
    
    Optional<Management> findByYear(Integer year);

    boolean existsByYear(Integer year);

    // 🚀 LA SOLUCIÓN AL N+1 EVOLUCIONADA Y CORREGIDA
    @Query("SELECT new com.universidad_nur.notasnurv3_api.dto.dashboard.ManagementSummaryDTO(" +
           "m.id, " +
           "m.year, " +
           "(CASE WHEN COUNT(s.id) = 0 THEN 'CONFIGURING' ELSE 'ACTIVE' END), " + // 🔥 Fix 1: Calculamos el status dinámicamente en SQL
           "COUNT(e.id), " +
           "COALESCE(SUM(CASE WHEN e.status = 'APPROVED' THEN 1.0 ELSE 0.0 END) / NULLIF(COUNT(e.id), 0) * 100.0, 0.0)) " +
           "FROM Management m " +
           "LEFT JOIN m.semesters s " + 
           "LEFT JOIN Subject sub ON sub.semester.id = s.id " + // 🔥 Fix 2: JOIN explícito ya que Semester no tiene la colección declarada
           "LEFT JOIN Enrollment e ON e.subject.id = sub.id " + // Cruzamos las inscripciones de las materias
           "GROUP BY m.id, m.year") // Agrupamos únicamente por campos reales de Management
    List<ManagementSummaryDTO> getManagementSummaries();
}