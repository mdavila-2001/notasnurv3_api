package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;

import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByStudentIdAndSubjectIdAndStatus(UUID studentId, Integer subjectId, EnrollmentStatus status);
    
    java.util.List<Enrollment> findBySubjectIdAndStatus(Integer subjectId, EnrollmentStatus status);
    
    java.util.List<Enrollment> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);
}
