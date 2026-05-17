package com.universidad_nur.notasnurv3_api.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardAdminDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardStudentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardTeacherDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.EnrolledSubjectDetailDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.GradeComponentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.ManagementSummaryDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.SubjectSummaryDTO;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Grade;
import com.universidad_nur.notasnurv3_api.entities.ProfileType;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter NEXT_EXAM_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("es-ES"));

    private final EnrollmentRepository enrollmentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final ManagementRepository managementRepository;
    private final UserRepository userRepository;
    private final UserDegreeRepository userDegreeRepository;
    private final EvaluationPlanRepository evaluationPlanRepository;
    private final SystemSettingService systemSettingService;

    @Transactional(readOnly = true)
    public DashboardAdminDTO getAdminDashboard() {
        // Conteo rápido de métricas base
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalSubjectsWithoutTeacher = subjectRepository.countByTeacherIsNull();
        long totalOpenActas = subjectRepository.countByRecordStatus(RecordStatus.ACTIVE);

        // Estudiantes en riesgo (Optimizado mediante Map local)
        List<Enrollment> activeEnrollments = enrollmentRepository.findByStatusWithDetails(EnrollmentStatus.ACTIVE);
        Map<UUID, Long> absencesByEnrollmentId = countAbsencesByEnrollmentIds(
                activeEnrollments.stream().map(Enrollment::getId).toList()
        );
        long studentsAtRiskCount = activeEnrollments.stream()
                .filter(enrollment -> isAtRisk(enrollment, absencesByEnrollmentId))
                .map(enrollment -> enrollment.getAcademicRecord().getUser().getId())
                .distinct()
                .count();

        // 🚀 RESOLUCIÓN DEL N+1: Una sola query calcula los conteos y los promedios históricos
        List<ManagementSummaryDTO> managementSummaries = managementRepository.getManagementSummaries();

        // 🚀 OPTIMIZACIÓN CRÍTICA: Calculamos la tasa global usando agregaciones directas desde el DTO obtenido, 
        // evitando el catastrófico enrollmentRepository.findAll() que saturaba la memoria RAM.
        long totalHistoricalStudents = 0;
        double combinedPassRateSum = 0.0;
        
        for (ManagementSummaryDTO summary : managementSummaries) {
            totalHistoricalStudents += summary.getStudentCount();
            combinedPassRateSum += (summary.getPassRate() * summary.getStudentCount());
        }
        
        double globalPassRate = totalHistoricalStudents == 0 ? 0.0 : (combinedPassRateSum / totalHistoricalStudents);

        return DashboardAdminDTO.builder()
                .totalStudents(totalStudents)
                .totalSubjectsWithoutTeacher(totalSubjectsWithoutTeacher)
                .totalOpenActas(totalOpenActas)
                .globalPassRate(globalPassRate)
                .studentsAtRiskCount(studentsAtRiskCount)
                .managements(managementSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardTeacherDTO getTeacherDashboard(String email) {
        Users teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado."));

        List<Subject> mySubjects = subjectRepository.findByTeacher_Id(teacher.getId());

        List<Enrollment> subjectEnrollments = mySubjects.isEmpty()
                ? List.of()
                : enrollmentRepository.findBySubjectTeacherIdWithDetails(teacher.getId());
        Map<Integer, List<Enrollment>> enrollmentsBySubjectId = subjectEnrollments.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getSubject().getId()));

        List<Integer> subjectIds = mySubjects.stream().map(Subject::getId).toList();
        Map<Integer, EvaluationPlan> evaluationPlansBySubjectId = evaluationPlanRepository.findBySubjectIdIn(subjectIds).stream()
                .collect(Collectors.toMap(plan -> plan.getSubject().getId(), plan -> plan));

        List<UUID> enrollmentIds = subjectEnrollments.stream().map(Enrollment::getId).toList();
        Map<UUID, Long> absencesByEnrollmentId = countAbsencesByEnrollmentIds(enrollmentIds);
        Map<UUID, Long> attendanceByEnrollmentId = countAttendanceByEnrollmentIds(enrollmentIds);

        int pendingActas = (int) mySubjects.stream().filter(s -> s.getRecordStatus() == RecordStatus.ACTIVE).count();

        double averageAttendance = calculateAverageAttendance(attendanceByEnrollmentId, absencesByEnrollmentId);
        double averageCourseGrade = calculateAverageCourseGrade(subjectEnrollments);
        String nextExamDate = resolveNextExamDate(mySubjects);

        List<SubjectSummaryDTO> subjectSummaries = mySubjects.stream()
                .map(s -> {
                    List<Enrollment> enrollments = enrollmentsBySubjectId.getOrDefault(s.getId(), List.of());
                    return SubjectSummaryDTO.builder()
                            .id(s.getId())
                            .code(s.getCode())
                            .name(s.getName())
                            .modality(s.getModality().name())
                            .studentCount(enrollments.size())
                            .progressPercentage(calculateProgressPercentage(s, enrollments, evaluationPlansBySubjectId.get(s.getId())))
                            .build();
                }).toList();

        return DashboardTeacherDTO.builder()
                .welcomeMessage("Te damos la bienvenida, " + teacher.getFullName())
                .averageAttendance(averageAttendance)
                .pendingActasCount(pendingActas)
                .averageCourseGrade(averageCourseGrade)
                .nextExamDate(nextExamDate)
                .subjects(subjectSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardStudentDTO getStudentDashboard(String email) {
        Users student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado."));

        List<Enrollment> enrollments = enrollmentRepository.findByAcademicRecord_UserId(student.getId());
        List<Enrollment> activeEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .toList();
        Map<UUID, Long> absencesByEnrollmentId = countAbsencesByEnrollmentIds(
                activeEnrollments.stream().map(Enrollment::getId).toList()
        );

        double gpa = enrollments.stream()
                .filter(e -> e.getFinalScore() != null)
                .mapToDouble(e -> e.getFinalScore().doubleValue())
                .average().orElse(0.0);

        List<EnrolledSubjectDetailDTO> details = activeEnrollments.stream()
                .map(e -> {
                    Subject s = e.getSubject();
                    long absences = absencesByEnrollmentId.getOrDefault(e.getId(), 0L);
                    int limit = systemSettingService.getAbsenceLimit(s.getModality());

                    List<Grade> grades = e.getGrades();
                    List<GradeComponentDTO> breakdown = grades.stream()
                            .map(g -> GradeComponentDTO.builder()
                                    .name(g.getComponent().getName())
                                    .score(g.getScore().doubleValue())
                                    .weight(g.getComponent().getWeight().doubleValue())
                                    .build())
                            .toList();

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
                            .atRisk(absences == limit - 1)
                            .gradeBreakdown(breakdown)
                            .build();
                }).toList();

        return DashboardStudentDTO.builder()
                .studentName(student.getFullName())
                .degreeName(resolveCurrentDegreeName(student.getId()))
                .currentGPA(gpa)
                .enrolledSubjects(details)
                .build();
    }

    private Map<UUID, Long> countAbsencesByEnrollmentIds(List<UUID> enrollmentIds) {
        if (enrollmentIds.isEmpty()) {
            return Map.of();
        }

        return attendanceRepository.countByEnrollmentIdsAndStatus(enrollmentIds, AttendanceStatus.ABSENT).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private Map<UUID, Long> countAttendanceByEnrollmentIds(List<UUID> enrollmentIds) {
        if (enrollmentIds.isEmpty()) {
            return Map.of();
        }

        return attendanceRepository.countByEnrollmentIds(enrollmentIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private boolean isAtRisk(Enrollment enrollment, Map<UUID, Long> absencesByEnrollmentId) {
        long absences = absencesByEnrollmentId.getOrDefault(enrollment.getId(), 0L);
        int limit = systemSettingService.getAbsenceLimit(enrollment.getSubject().getModality());
        return absences == limit - 1;
    }

    private double calculateAverageAttendance(Map<UUID, Long> attendanceByEnrollmentId, Map<UUID, Long> absencesByEnrollmentId) {
        long totalAttendanceRecords = attendanceByEnrollmentId.values().stream().mapToLong(Long::longValue).sum();
        long totalAbsences = absencesByEnrollmentId.values().stream().mapToLong(Long::longValue).sum();

        if (totalAttendanceRecords == 0L) {
            return 0.0;
        }

        return ((double) (totalAttendanceRecords - totalAbsences) / totalAttendanceRecords) * 100.0;
    }

    private double calculateAverageCourseGrade(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(Enrollment::getFinalScore)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double calculateProgressPercentage(Subject subject, List<Enrollment> enrollments, EvaluationPlan evaluationPlan) {
        if (subject.getRecordStatus() == RecordStatus.CLOSED) {
            return 100.0;
        }

        if (evaluationPlan == null || evaluationPlan.getComponents() == null || evaluationPlan.getComponents().isEmpty()) {
            return 0.0;
        }

        Set<Integer> gradedComponentIds = enrollments.stream()
                .flatMap(enrollment -> enrollment.getGrades().stream())
                .map(grade -> grade.getComponent().getId())
                .collect(Collectors.toSet());

        double totalWeight = evaluationPlan.getComponents().stream()
                .mapToDouble(component -> component.getWeight().doubleValue())
                .sum();

        if (totalWeight <= 0.0) {
            return 0.0;
        }

        double completedWeight = evaluationPlan.getComponents().stream()
                .filter(component -> gradedComponentIds.contains(component.getId()))
                .mapToDouble(component -> component.getWeight().doubleValue())
                .sum();

        return Math.min(100.0, (completedWeight / totalWeight) * 100.0);
    }

    private String resolveNextExamDate(List<Subject> subjects) {
        return subjects.stream()
                .map(Subject::getSemester)
                .map(Semester::getEndDate)
                .filter(endDate -> !endDate.isBefore(LocalDate.now()))
                .min(LocalDate::compareTo)
                .map(date -> date.format(NEXT_EXAM_FORMATTER).toUpperCase(Locale.ROOT))
                .orElse("N/A");
    }

    private String resolveCurrentDegreeName(UUID studentId) {
        return userDegreeRepository.findByUser_Id(studentId).stream()
                .filter(userDegree -> userDegree.getStatus() == AcademicStatus.ACTIVE)
                .filter(userDegree -> userDegree.getType() == ProfileType.STUDENT)
                .map(UserDegree::getDegree)
                .map(Degree::getName)
                .findFirst()
                .orElse("Carrera no asignada");
    }
}