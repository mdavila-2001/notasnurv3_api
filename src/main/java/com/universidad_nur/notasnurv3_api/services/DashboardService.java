package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.dashboard.*;
import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final ManagementRepository managementRepository;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;

    @Transactional(readOnly = true)
    public DashboardAdminDTO getAdminDashboard() {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalSubjectsWithoutTeacher = subjectRepository.findAll().stream()
                .filter(s -> s.getTeacher() == null).count();
        long totalOpenActas = subjectRepository.countByRecordStatus(RecordStatus.ACTIVE);

        List<Management> allManagements = managementRepository.findAll();
        List<ManagementSummaryDTO> managementSummaries = allManagements.stream()
                .map(m -> {
                    List<Enrollment> enrollments = enrollmentRepository.findBySubject_Semester_ManagementId(m.getId());
                    long passed = enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PASSED).count();
                    double passRate = enrollments.isEmpty() ? 0.0 : ((double) passed / enrollments.size()) * 100;
                    return ManagementSummaryDTO.builder()
                            .id(m.getId())
                            .year(m.getYear())
                            .status(m.getSemesters().isEmpty() ? "CONFIGURING" : "ACTIVE")
                            .studentCount(enrollments.size())
                            .passRate(passRate)
                            .build();
                }).collect(Collectors.toList());

        // Para simplificar, tomamos la tasa global de todas las inscripciones históricas
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        long totalPassed = allEnrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.PASSED).count();
        double globalPassRate = allEnrollments.isEmpty() ? 0.0 : ((double) totalPassed / allEnrollments.size()) * 100;

        return DashboardAdminDTO.builder()
                .totalStudents(totalStudents)
                .totalSubjectsWithoutTeacher(totalSubjectsWithoutTeacher)
                .totalOpenActas(totalOpenActas)
                .globalPassRate(globalPassRate)
                .studentsAtRiskCount(0) // Placeholder
                .managements(managementSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardTeacherDTO getTeacherDashboard(String email) {
        Users teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado."));

        List<Subject> mySubjects = subjectRepository.findAll().stream()
                .filter(s -> s.getTeacher() != null && s.getTeacher().getId().equals(teacher.getId()))
                .collect(Collectors.toList());

        int pendingActas = (int) mySubjects.stream().filter(s -> s.getRecordStatus() == RecordStatus.ACTIVE).count();

        List<SubjectSummaryDTO> subjectSummaries = mySubjects.stream()
                .map(s -> {
                    long studentCount = enrollmentRepository.findBySubjectId(s.getId()).size();
                    return SubjectSummaryDTO.builder()
                            .id(s.getId())
                            .code(s.getCode())
                            .name(s.getName())
                            .modality(s.getModality().name())
                            .studentCount((int) studentCount)
                            .progressPercentage(s.getRecordStatus() == RecordStatus.CLOSED ? 100.0 : 65.0) // Placeholder
                            .build();
                }).collect(Collectors.toList());

        return DashboardTeacherDTO.builder()
                .welcomeMessage("Bienvenido, " + teacher.getFullName())
                .averageAttendance(94.2) // Placeholder
                .pendingActasCount(pendingActas)
                .averageCourseGrade(78.4) // Placeholder
                .nextExamDate("14 OCT") // Placeholder
                .subjects(subjectSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStudentDTO getStudentDashboard(String email) {
        Users student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado."));

        List<Enrollment> enrollments = enrollmentRepository.findByAcademicRecord_UserId(student.getId());

        double gpa = enrollments.stream()
                .filter(e -> e.getFinalScore() != null)
                .mapToDouble(e -> e.getFinalScore().doubleValue())
                .average().orElse(0.0);

        List<EnrolledSubjectDetailDTO> details = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(e -> {
                    Subject s = e.getSubject();
                    long absences = attendanceRepository.countByEnrollmentIdAndStatus(e.getId(), AttendanceStatus.ABSENT);
                    int limit = switch (s.getModality()) {
                        case FACE_TO_FACE -> systemSettingService.getIntValue("ABSENCE_LIMIT_FACE_TO_FACE", 5);
                        case BLENDED -> systemSettingService.getIntValue("ABSENCE_LIMIT_BLENDED", 3);
                        case ONLINE -> 999;
                    };

                    List<Grade> grades = gradeRepository.findByEnrollmentId(e.getId());
                    List<GradeComponentDTO> breakdown = grades.stream()
                            .map(g -> GradeComponentDTO.builder()
                                    .name(g.getComponent().getName())
                                    .score(g.getScore().doubleValue())
                                    .weight(g.getComponent().getWeight().doubleValue())
                                    .build())
                            .collect(Collectors.toList());

                    double currentGrade = grades.stream().mapToDouble(g -> g.getScore().doubleValue()).sum();

                    return EnrolledSubjectDetailDTO.builder()
                            .subjectId(s.getId())
                            .subjectName(s.getName())
                            .subjectCode(s.getCode())
                            .teacherName(s.getTeacher() != null ? s.getTeacher().getFullName() : "N/A")
                            .category(s.getCategory() != null ? s.getCategory() : "GENERAL")
                            .currentGrade(currentGrade)
                            .absences((int) absences)
                            .absenceLimit(limit)
                            .atRisk(absences >= limit - 1)
                            .gradeBreakdown(breakdown)
                            .build();
                }).collect(Collectors.toList());

        return DashboardStudentDTO.builder()
                .studentName(student.getFullName())
                .degreeName("Bachelor of Computer Science") // Placeholder
                .currentGPA(gpa)
                .enrolledSubjects(details)
                .build();
    }
}
