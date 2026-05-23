package com.universidad_nur.notasnurv3_api.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.universidad_nur.notasnurv3_api.dto.GradeRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeResponse;
import com.universidad_nur.notasnurv3_api.entities.Components;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Grade;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.repositories.ComponentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.GradeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ComponentRepository componentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private GradeService gradeService;

    private Users teacher;
    private Enrollment enrollment;
    private Subject subject;
    private Semester semester;
    private Components component;
    private GradeRequest gradeRequest;
    private UUID enrollmentId;

    @BeforeEach
    void setUp() {
        enrollmentId = UUID.randomUUID();
        
        teacher = new Users();
        teacher.setId(UUID.randomUUID());
        teacher.setEmail("teacher@nur.edu");

        semester = new Semester();
        semester.setId(1);
        semester.setStartDate(LocalDate.now().minusMonths(4));
        semester.setEndDate(LocalDate.now().plusMonths(1)); // Active semester

        subject = new Subject();
        subject.setId(1);
        subject.setTeacher(teacher);
        subject.setSemester(semester);
        subject.setRecordStatus(RecordStatus.DRAFT);

        enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setSubject(subject);

        EvaluationPlan plan = new EvaluationPlan();
        plan.setSubject(subject);

        component = new Components();
        component.setId(1);
        component.setPlan(plan);
        component.setWeight(BigDecimal.valueOf(30));

        gradeRequest = new GradeRequest(enrollmentId, 1, BigDecimal.valueOf(25));
    }

    @Test
    void saveGrade_exito_cuandoEstaDentroDelLimiteTemporal() {
        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(systemSettingService.getIntValue("GRADING_GRACE_DAYS", 0)).thenReturn(0);
        when(componentRepository.findById(1)).thenReturn(Optional.of(component));
        when(gradeRepository.findByEnrollmentIdAndComponent_Id(enrollmentId, 1)).thenReturn(Optional.empty());
        
        Grade savedGrade = Grade.builder()
                .id(UUID.randomUUID())
                .enrollment(enrollment)
                .component(component)
                .teacher(teacher)
                .score(BigDecimal.valueOf(25))
                .build();
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        GradeResponse response = gradeService.saveGrade(gradeRequest, "teacher@nur.edu");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(25), response.score());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void saveGrade_lanzaInvalidOperationException_cuandoSeSuperaFechaLimite() {
        // Establecer fin de semestre en el pasado (ayer)
        semester.setEndDate(LocalDate.now().minusDays(1));

        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(systemSettingService.getIntValue("GRADING_GRACE_DAYS", 0)).thenReturn(0);

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> gradeService.saveGrade(gradeRequest, "teacher@nur.edu")
        );

        assertEquals("Se ha superado la fecha límite establecida para la carga de notas.", exception.getMessage());
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void saveGrade_exito_cuandoSeSuperaFechaLimitePeroDiasGraciaLoPermiten() {
        // Fin de semestre hace 2 días, pero hay 5 días de gracia (límite es en 3 días)
        semester.setEndDate(LocalDate.now().minusDays(2));

        when(userRepository.findByEmail("teacher@nur.edu")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(systemSettingService.getIntValue("GRADING_GRACE_DAYS", 0)).thenReturn(5);
        when(componentRepository.findById(1)).thenReturn(Optional.of(component));
        when(gradeRepository.findByEnrollmentIdAndComponent_Id(enrollmentId, 1)).thenReturn(Optional.empty());

        Grade savedGrade = Grade.builder()
                .id(UUID.randomUUID())
                .enrollment(enrollment)
                .component(component)
                .teacher(teacher)
                .score(BigDecimal.valueOf(25))
                .build();
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        GradeResponse response = gradeService.saveGrade(gradeRequest, "teacher@nur.edu");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(25), response.score());
        verify(gradeRepository).save(any(Grade.class));
    }
}
