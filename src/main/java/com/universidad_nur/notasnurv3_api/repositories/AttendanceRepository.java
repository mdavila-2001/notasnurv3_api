package com.universidad_nur.notasnurv3_api.repositories;

import com.universidad_nur.notasnurv3_api.entities.Attendance;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    Optional<Attendance> findByEnrollmentIdAndDate(UUID enrollmentId, LocalDate date);
    long countByEnrollmentIdAndStatus(UUID enrollmentId, AttendanceStatus status);
}
