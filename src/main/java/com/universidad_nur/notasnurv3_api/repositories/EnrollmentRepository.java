package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByAcademicRecordIdAndSubjectId(Integer userDegreeId, Integer subjectId);

    List<Enrollment> findBySubjectId(Integer subjectId);

    List<Enrollment> findBySubjectIdAndStatus(Integer subjectId, EnrollmentStatus status);

    List<Enrollment> findByAcademicRecord_UserIdAndStatus(UUID studentId, EnrollmentStatus status);
}
