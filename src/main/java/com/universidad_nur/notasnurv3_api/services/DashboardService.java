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

import com.universidad_nur.notasnurv3_api.entities.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.dashboard.CriticalSubjectDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardAdminDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardStudentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardTeacherDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.EnrolledSubjectDetailDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.GradeComponentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.ManagementSummaryDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.SubjectSummaryDTO;
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
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long totalSubjectsWithoutTeacher = subjectRepository.countByTeacherIsNull();
        long totalOpenActas = subjectRepository.countByRecordStatus(RecordStatus.ACTIVE);

        List<Enrollment> activeEnrollments = enrollmentRepository.findByStatusWithDetails(EnrollmentStatus.ACTIVE);
        Map<UUID, Long> absencesByEnrollmentId = countAbsencesByEnrollmentIds(
                activeEnrollments.stream().map(Enrollment::getId).toList()
        );
        long studentsAtRiskCount = activeEnrollments.stream()
                .filter(enrollment -> isAtRisk(enrollment, absencesByEnrollmentId))
                .map(enrollment -> enrollment.getAcademicRecord().getUser().getId())
                .distinct()
                .count();

        List<Management> allManagements = managementRepository.findAll();
        List<ManagementSummaryDTO> managementSummaries = allManagements.stream()
                .map(m -> {
                    long studentCount = enrollmentRepository.countBySubject_Semester_ManagementId(m.getId());
                    long passed = enrollmentRepository.countBySubject_Semester_ManagementIdAndStatus(m.getId(), EnrollmentStatus.PASSED);
                    double passRate = studentCount == 0 ? 0.0 : ((double) passed / studentCount) * 100;
                    return ManagementSummaryDTO.builder()
                            .id(m.getId())
                            .year(m.getYear())
                            .status(m.getSemesters().isEmpty() ? "CONFIGURING" : "ACTIVE")
                            .studentCount(studentCount)
                            .passRate(passRate)
                            .build();
                }).toList();

        long totalEnrollmentsCount = enrollmentRepository.count();
        long totalPassedCount = enrollmentRepository.countByStatus(EnrollmentStatus.PASSED);
        double globalPassRate = totalEnrollmentsCount == 0 ? 0.0 : ((double) totalPassedCount / totalEnrollmentsCount) * 100;

        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        Map<Subject, List<Enrollment>> enrollmentsBySubject = allEnrollments.stream()
                .filter(e -> e.getSubject() != null)
                .collect(Collectors.groupingBy(Enrollment::getSubject));

        List<CriticalSubjectDTO> criticalSubjects = enrollmentsBySubject.entrySet().stream()
                .map(entry -> {
                    Subject subject = entry.getKey();
                    List<Enrollment> subjectEnrollments = entry.getValue();
                    long total = subjectEnrollments.size();
                    long failed = subjectEnrollments.stream()
                            .filter(e -> e.getStatus() == EnrollmentStatus.FAILED || e.getStatus() == EnrollmentStatus.FAILED_BY_ATTENDANCE)
                            .count();
                    double failureRate = total == 0 ? 0.0 : ((double) failed / total) * 100.0;
                    String teacherName = subject.getTeacher() != null ? subject.getTeacher().getFullName() : "Sin docente asignado";
                    String statusStr = subject.getRecordStatus() == RecordStatus.CLOSED ? "CERRADA" : "ACTIVA";

                    return CriticalSubjectDTO.builder()
                            .id(String.valueOf(subject.getId()))
                            .code(subject.getCode())
                            .name(subject.getName())
                            .teacherName(teacherName)
                            .failureRate(Math.round(failureRate * 10.0) / 10.0)
                            .status(statusStr)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getFailureRate(), a.getFailureRate()))
                .limit(5)
                .toList();

        return DashboardAdminDTO.builder()
                .totalStudents(totalStudents)
                .totalSubjectsWithoutTeacher(totalSubjectsWithoutTeacher)
                .totalOpenActas(totalOpenActas)
                .globalPassRate(globalPassRate)
                .studentsAtRiskCount(studentsAtRiskCount)
                .managements(managementSummaries)
                .criticalSubjects(criticalSubjects)
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

        double averageCourseGrade = calculateAverageCourseGrade(subjectEnrollments);

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
                .pendingActasCount(pendingActas)
                .averageCourseGrade(averageCourseGrade)
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