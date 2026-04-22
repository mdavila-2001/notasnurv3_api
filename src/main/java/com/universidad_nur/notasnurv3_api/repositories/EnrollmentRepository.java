package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByStudentIdAndSubjectId(UUID studentId, Integer subjectId);
    
    java.util.List<Enrollment> findBySubjectId(Integer subjectId);
    
    java.util.List<Enrollment> findByStudentId(UUID studentId);
}
