package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Attendance;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    Optional<Attendance> findByEnrollmentIdAndDate(UUID enrollmentId, LocalDate date);
    long countByEnrollmentIdAndStatus(UUID enrollmentId, AttendanceStatus status);

        @Query("""
                        SELECT a.enrollment.id, COUNT(a)
                        FROM Attendance a
                        WHERE a.enrollment.id IN :enrollmentIds
                            AND a.status = :status
                        GROUP BY a.enrollment.id
                        """)
        java.util.List<Object[]> countByEnrollmentIdsAndStatus(@Param("enrollmentIds") Collection<UUID> enrollmentIds,
                                                                                                                     @Param("status") AttendanceStatus status);
}
