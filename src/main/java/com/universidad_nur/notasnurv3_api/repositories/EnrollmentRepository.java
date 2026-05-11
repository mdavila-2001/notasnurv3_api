package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByAcademicRecordIdAndSubjectId(Integer userDegreeId, Integer subjectId);

        @Query("""
            SELECT DISTINCT e
            FROM Enrollment e
            LEFT JOIN FETCH e.academicRecord ar
            LEFT JOIN FETCH ar.user
            LEFT JOIN FETCH e.subject s
                LEFT JOIN FETCH s.semester
            LEFT JOIN FETCH e.grades g
            LEFT JOIN FETCH g.components
            WHERE s.id = :subjectId
            """)
        List<Enrollment> findBySubjectId(@Param("subjectId") Integer subjectId);

    List<Enrollment> findBySubjectIdAndStatus(Integer subjectId, EnrollmentStatus status);

    List<Enrollment> findByAcademicRecord_UserIdAndStatus(UUID studentId, EnrollmentStatus status);

        @Query("""
            SELECT DISTINCT e
            FROM Enrollment e
            LEFT JOIN FETCH e.academicRecord ar
            LEFT JOIN FETCH ar.user
            LEFT JOIN FETCH e.subject s
                LEFT JOIN FETCH s.semester
            LEFT JOIN FETCH e.grades g
            LEFT JOIN FETCH g.components
            WHERE ar.user.id = :studentId
            """)
        List<Enrollment> findByAcademicRecord_UserId(@Param("studentId") UUID studentId);

            @Query("""
                SELECT DISTINCT e
                FROM Enrollment e
                LEFT JOIN FETCH e.academicRecord ar
                LEFT JOIN FETCH ar.user
                LEFT JOIN FETCH e.subject s
                LEFT JOIN FETCH s.semester
                LEFT JOIN FETCH e.grades g
                LEFT JOIN FETCH g.components
                WHERE e.status = :status
                """)
            List<Enrollment> findByStatusWithDetails(@Param("status") EnrollmentStatus status);

            @Query("""
                SELECT DISTINCT e
                FROM Enrollment e
                LEFT JOIN FETCH e.academicRecord ar
                LEFT JOIN FETCH ar.user
                LEFT JOIN FETCH e.subject s
                LEFT JOIN FETCH s.semester
                LEFT JOIN FETCH e.grades g
                LEFT JOIN FETCH g.components
                WHERE s.teacher.id = :teacherId
                """)
            List<Enrollment> findBySubjectTeacherIdWithDetails(@Param("teacherId") UUID teacherId);

        @Query("""
            SELECT DISTINCT e
            FROM Enrollment e
            JOIN FETCH e.subject s
            WHERE s.semester.management.id = :managementId
            """)
        List<Enrollment> findBySubject_Semester_ManagementId(@Param("managementId") Integer managementId);

            @Query("""
                SELECT e.subject.id, COUNT(e)
                FROM Enrollment e
                WHERE e.subject.id IN :subjectIds
                GROUP BY e.subject.id
                """)
            List<Object[]> countBySubjectIds(@Param("subjectIds") Collection<Integer> subjectIds);

        long countBySubjectId(Integer subjectId);
}
