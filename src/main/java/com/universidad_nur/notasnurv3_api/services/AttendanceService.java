package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.AttendanceBulkRequest;
import com.universidad_nur.notasnurv3_api.dto.StudentAttendance;
import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SystemSettingService systemSettingService;

    @Transactional
    public void saveBulkAttendance(AttendanceBulkRequest request, String teacherEmail) {
        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidOperationException("No se puede registrar asistencia en fechas futuras.");
        }

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        if (subject.getRecordStatus() == RecordStatus.CLOSED) {
            throw new InvalidOperationException("No se puede registrar asistencia si la materia está en estado CLOSED.");
        }

        if (subject.getTeacher() == null || !subject.getTeacher().getEmail().equalsIgnoreCase(teacherEmail)) {
            throw new UnauthorizedAccessException("No tienes permisos para registrar asistencia en esta materia.");
        }

        for (StudentAttendance record : request.records()) {
            Enrollment enrollment = enrollmentRepository.findById(record.enrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada para ID: " + record.enrollmentId()));

            if (!enrollment.getSubject().getId().equals(subject.getId())) {
                throw new InvalidOperationException("La inscripción " + record.enrollmentId() + " no pertenece a la materia.");
            }

            // Upsert Attendance
            Optional<Attendance> existingOpt = attendanceRepository.findByEnrollmentIdAndDate(record.enrollmentId(), request.date());
            Attendance attendance;
            if (existingOpt.isPresent()) {
                attendance = existingOpt.get();
                attendance.setStatus(record.status());
            } else {
                attendance = Attendance.builder()
                        .enrollment(enrollment)
                        .date(request.date())
                        .status(record.status())
                        .build();
            }
            attendanceRepository.save(attendance);

            // Regla del Killer (HU 16)
            if (record.status() == AttendanceStatus.ABSENT) {
                long totalAbsences = attendanceRepository.countByEnrollmentIdAndStatus(record.enrollmentId(), AttendanceStatus.ABSENT);
                int limit = getAbsenceLimit(subject.getModality());

                if (totalAbsences > limit && enrollment.getStatus() != EnrollmentStatus.FAILED_BY_ATTENDANCE) {
                    enrollment.setStatus(EnrollmentStatus.FAILED_BY_ATTENDANCE);
                    enrollmentRepository.save(enrollment);
                }
            }
        }
    }

    private int getAbsenceLimit(Modality modality) {
        return switch (modality) {
            case FACE_TO_FACE -> systemSettingService.getIntValue("ABSENCE_LIMIT_FACE_TO_FACE", 5);
            case BLENDED -> systemSettingService.getIntValue("ABSENCE_LIMIT_BLENDED", 3);
            case ONLINE -> systemSettingService.getIntValue("ABSENCE_LIMIT_ONLINE", 999);
        };
    }
}
