package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationPlanServiceTest {
    @Mock
    private EvaluationPlanRepository evaluationPlanRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private EvaluationPlanService evaluationPlanService;

    private Subject mockSubject;
    private Users mockTeacher;

    @BeforeEach
    void setUp() {
        mockTeacher = new Users();
        mockTeacher.setEmail("profesor@nur.edu.bo");

        mockSubject = new Subject();
        mockSubject.setId(1);
        mockSubject.setTeacher(mockTeacher);
    }

    @Test
    void getBySubject_ShouldReturnPlan_WhenPlanExists() {
        EvaluationPlan mockPlan = new EvaluationPlan();
        mockPlan.setId(100);
        mockPlan.setSubject(mockSubject);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(mockPlan));

        EvaluationPlanResponse response = evaluationPlanService.getBySubject(1, "profesor@nur.edu.bo");

        assertNotNull(response);
        assertEquals(100, response.id());
    }

    @Test
    void getBySubject_ShouldThrow404_WhenPlanDoesNotExist() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> evaluationPlanService.getBySubject(1, "profesor@nur.edu.bo"));
    }

    @Test
    void getBySubject_ShouldThrow403_WhenTeacherDoesNotMatch() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));

        assertThrows(UnauthorizedAccessException.class,
                () -> evaluationPlanService.getBySubject(1, "hacker@nur.edu.bo"));
    }

    @Test
    void createForSubject_ShouldReturnExistingPlan_WhenPlanAlreadyExists() {
        EvaluationPlan existingPlan = new EvaluationPlan();
        existingPlan.setId(100);
        existingPlan.setSubject(mockSubject);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(existingPlan));

        EvaluationPlanResponse response = evaluationPlanService.createForSubject(1, "profesor@nur.edu.bo");

        assertNotNull(response);
        assertEquals(100, response.id());
        verify(evaluationPlanRepository, never()).save(any(EvaluationPlan.class));
    }

    @Test
    void createForSubject_ShouldCreatePlan_WhenPlanDoesNotExist() {
        EvaluationPlan createdPlan = new EvaluationPlan();
        createdPlan.setId(200);
        createdPlan.setSubject(mockSubject);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.empty());
        when(evaluationPlanRepository.save(any(EvaluationPlan.class))).thenReturn(createdPlan);

        EvaluationPlanResponse response = evaluationPlanService.createForSubject(1, "profesor@nur.edu.bo");

        assertNotNull(response);
        assertEquals(200, response.id());
        verify(evaluationPlanRepository, times(1)).save(any(EvaluationPlan.class));
    }

    @Test
    void createForSubject_ShouldPropagateDataIntegrityViolation_WhenConcurrentConflictOccurs() {
        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.empty());
        when(evaluationPlanRepository.save(any(EvaluationPlan.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(DataIntegrityViolationException.class,
                () -> evaluationPlanService.createForSubject(1, "profesor@nur.edu.bo"));

        verify(evaluationPlanRepository, times(1)).save(any(EvaluationPlan.class));
    }

    @Test
    void activatePlan_ShouldThrowException_WhenPlanIsEmpty() {
        EvaluationPlan mockPlan = new EvaluationPlan();
        mockPlan.setId(100);
        mockPlan.setSubject(mockSubject);
        mockPlan.setComponents(java.util.Collections.emptyList());

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(mockPlan));

        com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException exception = assertThrows(
                com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException.class,
                () -> evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo")
        );

        assertEquals("No se puede activar un plan de evaluación sin componentes.", exception.getMessage());
    }

    @Test
    void activatePlan_ShouldThrowException_WhenPlanHasNullComponents() {
        EvaluationPlan mockPlan = new EvaluationPlan();
        mockPlan.setId(100);
        mockPlan.setSubject(mockSubject);
        mockPlan.setComponents(null);

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(mockPlan));

        com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException exception = assertThrows(
                com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException.class,
                () -> evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo")
        );

        assertEquals("No se puede activar un plan de evaluación sin componentes.", exception.getMessage());
    }

    @Test
    void activatePlan_ShouldThrowException_WhenTotalWeightIsNot100() {
        EvaluationPlan mockPlan = new EvaluationPlan();
        mockPlan.setId(100);
        mockPlan.setSubject(mockSubject);
        
        com.universidad_nur.notasnurv3_api.entities.Components component = com.universidad_nur.notasnurv3_api.entities.Components.builder()
                .weight(new java.math.BigDecimal("90"))
                .build();
        mockPlan.setComponents(java.util.Arrays.asList(component));

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(mockPlan));

        com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException exception = assertThrows(
                com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException.class,
                () -> evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo")
        );

        assertEquals("La suma de las ponderaciones debe ser exactamente 100.", exception.getMessage());
    }

    @Test
    void activatePlan_ShouldActivatePlan_WhenPlanIsValid() {
        EvaluationPlan mockPlan = new EvaluationPlan();
        mockPlan.setId(100);
        mockPlan.setSubject(mockSubject);

        com.universidad_nur.notasnurv3_api.entities.Components c1 = com.universidad_nur.notasnurv3_api.entities.Components.builder().weight(new java.math.BigDecimal("50")).build();
        com.universidad_nur.notasnurv3_api.entities.Components c2 = com.universidad_nur.notasnurv3_api.entities.Components.builder().weight(new java.math.BigDecimal("50")).build();
        mockPlan.setComponents(java.util.Arrays.asList(c1, c2));

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(mockPlan));

        evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo");

        assertEquals(com.universidad_nur.notasnurv3_api.entities.RecordStatus.ACTIVE, mockSubject.getRecordStatus());
        verify(subjectRepository, times(1)).save(mockSubject);
    }
}
