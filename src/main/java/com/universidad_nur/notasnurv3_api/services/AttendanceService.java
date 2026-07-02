package com.universidad_nur.notasnurv3_api.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.AttendanceAbsencesResponse;
import com.universidad_nur.notasnurv3_api.dto.AttendanceBulkRequest;
import com.universidad_nur.notasnurv3_api.dto.AttendanceRecordResponse;
import com.universidad_nur.notasnurv3_api.dto.StudentAbsence;
import com.universidad_nur.notasnurv3_api.dto.StudentAttendance;
import com.universidad_nur.notasnurv3_api.entities.Attendance;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.AuditLog;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.AuditLogRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;
    private final AuditLogRepository auditLogRepository;

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

        java.util.List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subject.getId());
        java.util.Map<java.util.UUID, Enrollment> enrollmentMap = enrollments.stream()
                .collect(java.util.stream.Collectors.toMap(Enrollment::getId, e -> e));

        java.util.List<java.util.UUID> enrollmentIdsInRequest = request.records().stream()
                .map(StudentAttendance::enrollmentId)
                .toList();


        for (java.util.UUID rId : enrollmentIdsInRequest) {
            if (!enrollmentMap.containsKey(rId)) {
                throw new InvalidOperationException("La inscripción " + rId + " no pertenece a la materia.");
            }
        }

        java.util.List<Attendance> existingAttendances = attendanceRepository.findByEnrollmentIdInAndDate(enrollmentIdsInRequest, request.date());
        java.util.Map<java.util.UUID, Attendance> existingAttendanceMap = existingAttendances.stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getEnrollment().getId(), a -> a));

        java.util.List<Attendance> attendancesToSave = new java.util.ArrayList<>();
        java.util.List<Attendance> newAttendances = new java.util.ArrayList<>();
        java.util.Map<java.util.UUID, String> oldStatusMap = new java.util.HashMap<>();

        for (StudentAttendance item : request.records()) {
            Enrollment enrollment = enrollmentMap.get(item.enrollmentId());
            Attendance attendance = existingAttendanceMap.get(item.enrollmentId());

            if (attendance != null) {
                String oldStatus = attendance.getStatus().name();
                if (!oldStatus.equals(item.status().name())) {
                    oldStatusMap.put(item.enrollmentId(), oldStatus);
                }
                attendance.setStatus(item.status());
            } else {
                attendance = Attendance.builder()
                        .enrollment(enrollment)
                        .date(request.date())
                        .status(item.status())
                        .build();
                newAttendances.add(attendance);
            }
            attendancesToSave.add(attendance);
        }

        java.util.List<Attendance> savedAttendances = attendanceRepository.saveAll(attendancesToSave);


        for (Attendance saved : savedAttendances) {
            java.util.UUID eId = saved.getEnrollment().getId();
            String jsonNewValue = "{\"status\": \"" + saved.getStatus().name() + "\"}";


            if (newAttendances.stream().anyMatch(a -> a.getEnrollment().getId().equals(eId))) {
                auditLogRepository.save(AuditLog.builder()
                        .affectedTable("attendance")
                        .recordId(saved.getId())
                        .userId(teacher.getId())
                        .action("CREATE")
                        .oldValue(null)
                        .newValue(jsonNewValue)
                        .ipAddress("127.0.0.1")
                        .build());
            } else if (oldStatusMap.containsKey(eId)) {
                String jsonOldValue = "{\"status\": \"" + oldStatusMap.get(eId) + "\"}";
                auditLogRepository.save(AuditLog.builder()
                        .affectedTable("attendance")
                        .recordId(saved.getId())
                        .userId(teacher.getId())
                        .action("UPDATE")
                        .oldValue(jsonOldValue)
                        .newValue(jsonNewValue)
                        .ipAddress("127.0.0.1")
                        .build());
            }
        }
        java.util.List<java.util.UUID> allEnrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        java.util.Map<java.util.UUID, Long> absencesCountMap = countAbsencesByEnrollmentIds(allEnrollmentIds);

        java.util.List<Enrollment> enrollmentsToUpdate = new java.util.ArrayList<>();
        int limit = systemSettingService.getAbsenceLimit(subject.getModality());

        for (Enrollment enrollment : enrollments) {
            long totalAbsences = absencesCountMap.getOrDefault(enrollment.getId(), 0L);

            if (totalAbsences > limit) {
                if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                    enrollment.setStatus(EnrollmentStatus.FAILED_BY_ATTENDANCE);
                    enrollmentsToUpdate.add(enrollment);
                }
            } else {
                if (enrollment.getStatus() == EnrollmentStatus.FAILED_BY_ATTENDANCE) {
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollmentsToUpdate.add(enrollment);
                }
            }
        }

        if (!enrollmentsToUpdate.isEmpty()) {
            enrollmentRepository.saveAll(enrollmentsToUpdate);
        }
    }

    @Transactional(readOnly = true)
    public AttendanceAbsencesResponse getSubjectAbsences(Integer subjectId, String teacherEmail) {
        Subject subject = getValidatedSubjectForTeacher(subjectId, teacherEmail);
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subject.getId());
        List<UUID> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        Map<UUID, Long> absencesCountMap = countAbsencesByEnrollmentIds(enrollmentIds);

        List<StudentAbsence> students = enrollments.stream()
                .map(enrollment -> {
                    Users student = enrollment.getAcademicRecord().getUser();
                    long absencesCount = absencesCountMap.getOrDefault(enrollment.getId(), 0L);
                    return new StudentAbsence(
                            enrollment.getId(),
                            student.getId(),
                            student.getFullName(),
                            (int) absencesCount
                    );
                })
                .toList();

        int absenceLimit = systemSettingService.getAbsenceLimit(subject.getModality());
        return new AttendanceAbsencesResponse(students, absenceLimit);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getAttendanceBySubjectAndDate(Integer subjectId, LocalDate date, String teacherEmail) {
        Subject subject = getValidatedSubjectForTeacher(subjectId, teacherEmail);
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subject.getId());
        List<UUID> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        Map<UUID, Attendance> attendanceByEnrollmentId = attendanceRepository.findByEnrollmentIdInAndDate(enrollmentIds, date).stream()
                .collect(java.util.stream.Collectors.toMap(
                        attendance -> attendance.getEnrollment().getId(),
                        attendance -> attendance
                ));

        return enrollments.stream()
                .map(enrollment -> {
                    Attendance attendance = attendanceByEnrollmentId.get(enrollment.getId());
                    if (attendance == null) {
                        return null;
                    }

                    Users student = enrollment.getAcademicRecord().getUser();
                    return new AttendanceRecordResponse(
                            enrollment.getId(),
                            student.getId(),
                            student.getFullName(),
                            attendance.getStatus().name(),
                            attendance.getDate()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private java.util.Map<java.util.UUID, Long> countAbsencesByEnrollmentIds(java.util.List<java.util.UUID> enrollmentIds) {
        if (enrollmentIds.isEmpty()) {
            return java.util.Map.of();
        }

        return attendanceRepository.countByEnrollmentIdsAndStatus(enrollmentIds, AttendanceStatus.ABSENT).stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (java.util.UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private Subject getValidatedSubjectForTeacher(Integer subjectId, String teacherEmail) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("No tienes permisos para acceder a esta materia.");
        }

        return subject;
    }
}