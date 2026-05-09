package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardAdminDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardStudentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardTeacherDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.SubjectSummaryDTO;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.AttendanceStatus;
import com.universidad_nur.notasnurv3_api.entities.Component;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Modality;
import com.universidad_nur.notasnurv3_api.entities.ProfileType;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.entities.Grade;
import com.universidad_nur.notasnurv3_api.repositories.AttendanceRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ManagementRepository managementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDegreeRepository userDegreeRepository;

    @Mock
    private EvaluationPlanRepository evaluationPlanRepository;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getAdminDashboard_shouldCountStudentsAtRiskFromCurrentThreshold() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        Users student = user(studentId, "Laura", "Mendoza", Role.STUDENT);
        Subject subject = Subject.builder()
                .id(10)
                .modality(Modality.FACE_TO_FACE)
                .recordStatus(RecordStatus.ACTIVE)
                .teacher(user(UUID.randomUUID(), "Ana", "Pérez", Role.TEACHER))
                .semester(semester(LocalDate.now().plusDays(10)))
                .build();

        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .academicRecord(UserDegree.builder().user(student).build())
                .subject(subject)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        when(userRepository.countByRole(Role.STUDENT)).thenReturn(1L);
        when(subjectRepository.countByTeacherIsNull()).thenReturn(0L);
        when(subjectRepository.countByRecordStatus(RecordStatus.ACTIVE)).thenReturn(1L);
        when(enrollmentRepository.findByStatusWithDetails(EnrollmentStatus.ACTIVE)).thenReturn(List.of(enrollment));
        when(attendanceRepository.countByEnrollmentIdsAndStatus(List.of(enrollmentId), AttendanceStatus.ABSENT))
                .thenReturn(List.<Object[]>of(new Object[]{enrollmentId, 4L}));
        when(systemSettingService.getAbsenceLimit(Modality.FACE_TO_FACE)).thenReturn(5);
        when(managementRepository.findAll()).thenReturn(List.of());
        when(enrollmentRepository.findAll()).thenReturn(List.of());

        DashboardAdminDTO dashboard = dashboardService.getAdminDashboard();

        assertNotNull(dashboard);
        assertEquals(1L, dashboard.getStudentsAtRiskCount());
    }

    @Test
    void getTeacherDashboard_shouldBuildDynamicMetrics() {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId1 = UUID.randomUUID();
        UUID enrollmentId2 = UUID.randomUUID();

        Users teacher = user(teacherId, "Ana", "Pérez", Role.TEACHER);
        Users student = user(studentId, "Mario", "López", Role.STUDENT);

        LocalDate endDate = LocalDate.now().plusDays(30);
        Subject subject = Subject.builder()
                .id(101)
                .code("INF-101")
                .name("Programación I")
                .modality(Modality.FACE_TO_FACE)
                .recordStatus(RecordStatus.ACTIVE)
                .teacher(teacher)
                .semester(semester(endDate))
                .category("CORE")
                .build();

        Component component1 = Component.builder().id(1).name("Parcial 1").weight(new BigDecimal("40.00")).build();
        Component component2 = Component.builder().id(2).name("Parcial 2").weight(new BigDecimal("60.00")).build();

        EvaluationPlan plan = EvaluationPlan.builder()
                .subject(subject)
                .components(List.of(component1, component2))
                .build();

        Grade grade1 = Grade.builder().component(component1).teacher(teacher).score(new BigDecimal("40.00")).build();
        Grade grade2 = Grade.builder().component(component2).teacher(teacher).score(new BigDecimal("60.00")).build();

        Enrollment enrollment1 = Enrollment.builder()
                .id(enrollmentId1)
                .subject(subject)
                .academicRecord(UserDegree.builder().user(student).build())
                .status(EnrollmentStatus.ACTIVE)
                .finalScore(70)
                .grades(List.of(grade1))
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .id(enrollmentId2)
                .subject(subject)
                .academicRecord(UserDegree.builder().user(student).build())
                .status(EnrollmentStatus.ACTIVE)
                .finalScore(90)
                .grades(List.of(grade2))
                .build();

        when(userRepository.findByEmail("ana@nur.edu")).thenReturn(Optional.of(teacher));
        when(subjectRepository.findByTeacher_Id(teacherId)).thenReturn(List.of(subject));
        when(enrollmentRepository.findBySubjectTeacherIdWithDetails(teacherId)).thenReturn(List.of(enrollment1, enrollment2));
        when(evaluationPlanRepository.findBySubjectIdIn(List.of(subject.getId()))).thenReturn(List.of(plan));
        when(attendanceRepository.countByEnrollmentIds(List.of(enrollmentId1, enrollmentId2)))
                .thenReturn(List.<Object[]>of(
                        new Object[]{enrollmentId1, 4L},
                        new Object[]{enrollmentId2, 4L}
                ));
        when(attendanceRepository.countByEnrollmentIdsAndStatus(List.of(enrollmentId1, enrollmentId2), AttendanceStatus.ABSENT))
                .thenReturn(List.<Object[]>of(
                        new Object[]{enrollmentId1, 1L},
                        new Object[]{enrollmentId2, 1L}
                ));

        DashboardTeacherDTO dashboard = dashboardService.getTeacherDashboard("ana@nur.edu");

        assertNotNull(dashboard);
        assertEquals("Te damos la bienvenida, Ana Pérez", dashboard.getWelcomeMessage());
        assertEquals(75.0, dashboard.getAverageAttendance(), 0.001);
        assertEquals(80.0, dashboard.getAverageCourseGrade(), 0.001);
        assertEquals(1, dashboard.getPendingActasCount());
        assertEquals(resolveExpectedDate(endDate), dashboard.getNextExamDate());

        List<SubjectSummaryDTO> subjects = dashboard.getSubjects();
        assertEquals(1, subjects.size());
        assertEquals(2, subjects.get(0).getStudentCount());
        assertEquals(100.0, subjects.get(0).getProgressPercentage(), 0.001);
    }

    @Test
    void getStudentDashboard_shouldResolveActiveDegreeName() {
        UUID studentId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        Users student = user(studentId, "Carlos", "Ruiz", Role.STUDENT);
        Subject subject = Subject.builder()
                .id(202)
                .code("MAT-202")
                .name("Matemática II")
                .modality(Modality.FACE_TO_FACE)
                .recordStatus(RecordStatus.ACTIVE)
                .teacher(user(UUID.randomUUID(), "Sofía", "García", Role.TEACHER))
                .semester(semester(LocalDate.now().plusDays(20)))
                .build();

        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .subject(subject)
                .academicRecord(UserDegree.builder().user(student).build())
                .status(EnrollmentStatus.ACTIVE)
                .finalScore(85)
                .grades(List.of())
                .build();

        Degree degree = Degree.builder().id(1).name("Ingeniería de Sistemas").build();
        UserDegree activeDegree = UserDegree.builder()
                .id(99)
                .user(student)
                .degree(degree)
                .type(ProfileType.STUDENT)
                .status(AcademicStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("carlos@nur.edu")).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByAcademicRecord_UserId(studentId)).thenReturn(List.of(enrollment));
        when(attendanceRepository.countByEnrollmentIdsAndStatus(List.of(enrollmentId), AttendanceStatus.ABSENT))
                .thenReturn(List.<Object[]>of(new Object[]{enrollmentId, 2L}));
        when(systemSettingService.getAbsenceLimit(Modality.FACE_TO_FACE)).thenReturn(5);
        when(userDegreeRepository.findByUser_Id(studentId)).thenReturn(List.of(activeDegree));

        DashboardStudentDTO dashboard = dashboardService.getStudentDashboard("carlos@nur.edu");

        assertNotNull(dashboard);
        assertEquals("Carlos Ruiz", dashboard.getStudentName());
        assertEquals("Ingeniería de Sistemas", dashboard.getDegreeName());
        assertEquals(85.0, dashboard.getCurrentGPA(), 0.001);
        assertEquals(1, dashboard.getEnrolledSubjects().size());
        assertFalse(dashboard.getEnrolledSubjects().get(0).isAtRisk());
        assertTrue(dashboard.getEnrolledSubjects().get(0).getAbsences() < dashboard.getEnrolledSubjects().get(0).getAbsenceLimit());
    }

    private Users user(UUID id, String name, String lastName, Role role) {
        return Users.builder()
                .id(id)
                .name(name)
                .lastName(lastName)
                .role(role)
                .build();
    }

    private Semester semester(LocalDate endDate) {
        return Semester.builder()
                .id(1)
                .number(1)
                .startDate(endDate.minusMonths(4))
                .endDate(endDate)
                .management(Management.builder().id(1).year(2026).build())
                .build();
    }

    private String resolveExpectedDate(LocalDate endDate) {
        return endDate.format(DateTimeFormatter.ofPattern("dd MMM", new Locale("es", "ES"))).toUpperCase(Locale.ROOT);
    }
}