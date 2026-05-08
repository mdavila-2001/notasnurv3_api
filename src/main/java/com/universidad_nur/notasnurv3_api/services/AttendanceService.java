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
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;

    @Transactional(rollbackFor = Exception.class)
    public void saveBulkAttendance(AttendanceBulkRequest request, String teacherEmail) {
        if (request.date().isAfter(LocalDate.now())) {
            throw new InvalidOperationException("No se puede registrar asistencia en fechas futuras.");
        }

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        if (subject.getRecordStatus() == RecordStatus.CLOSED) {
            throw new InvalidOperationException("No se puede registrar asistencia si la materia está en estado CLOSED.");
        }

        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("No tienes permisos para registrar asistencia en esta materia.");
        }

        for (StudentAttendance item : request.records()) {
            processStudentAttendance(item, request.date(), subject.getId(), subject.getModality());
        }
    }

    private void processStudentAttendance(StudentAttendance item, LocalDate date, Integer subjectId, Modality modality) {
        Enrollment enrollment = enrollmentRepository.findById(item.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada para ID: " + item.enrollmentId()));

        if (!enrollment.getSubject().getId().equals(subjectId)) {
            throw new InvalidOperationException("La inscripción " + item.enrollmentId() + " no pertenece a la materia.");
        }

        // Upsert Attendance
        Optional<Attendance> existingOpt = attendanceRepository.findByEnrollmentIdAndDate(item.enrollmentId(), date);
        Attendance attendance;
        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            attendance.setStatus(item.status());
        } else {
            attendance = Attendance.builder()
                    .enrollment(enrollment)
                    .date(date)
                    .status(item.status())
                    .build();
        }
        attendanceRepository.save(attendance);

        reconcileEnrollmentStatus(enrollment, modality);
    }

    private void reconcileEnrollmentStatus(Enrollment enrollment, Modality modality) {
        long totalAbsences = attendanceRepository.countByEnrollmentIdAndStatus(enrollment.getId(), AttendanceStatus.ABSENT);
        int limit = systemSettingService.getAbsenceLimit(modality);

        if (totalAbsences > limit) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                enrollment.setStatus(EnrollmentStatus.FAILED_BY_ATTENDANCE);
                enrollmentRepository.save(enrollment);
            }
            return;
        }

        if (enrollment.getStatus() == EnrollmentStatus.FAILED_BY_ATTENDANCE) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollmentRepository.save(enrollment);
        }
    }
}
