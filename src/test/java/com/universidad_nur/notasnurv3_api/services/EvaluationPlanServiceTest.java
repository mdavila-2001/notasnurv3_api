package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.entities.Components;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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
    void activatePlan_ShouldThrowInvalidOperation_WhenPlanHasNoComponents() {
        EvaluationPlan emptyPlan = new EvaluationPlan();
        emptyPlan.setId(100);
        emptyPlan.setSubject(mockSubject);
        emptyPlan.setComponents(Collections.emptyList());

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(emptyPlan));

        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo"));

        assertEquals("No se puede activar un plan de evaluación sin componentes.", exception.getMessage());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void activatePlan_ShouldThrowInvalidOperation_WhenTotalWeightIsNotOneHundred() {
        EvaluationPlan incompletePlan = new EvaluationPlan();
        incompletePlan.setId(100);
        incompletePlan.setSubject(mockSubject);
        incompletePlan.setComponents(List.of(
                Components.builder().weight(new BigDecimal("40.00")).build(),
                Components.builder().weight(new BigDecimal("50.00")).build()
        ));

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(incompletePlan));

        InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                () -> evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo"));

        assertEquals("La suma de las ponderaciones debe ser exactamente 100.", exception.getMessage());
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void activatePlan_ShouldSetSubjectAsActive_WhenTotalWeightIsExactlyOneHundred() {
        EvaluationPlan validPlan = new EvaluationPlan();
        validPlan.setId(100);
        validPlan.setSubject(mockSubject);
        validPlan.setComponents(List.of(
                Components.builder().weight(new BigDecimal("40.00")).build(),
                Components.builder().weight(new BigDecimal("60.00")).build()
        ));

        when(subjectRepository.findById(1)).thenReturn(Optional.of(mockSubject));
        when(evaluationPlanRepository.findBySubjectId(1)).thenReturn(Optional.of(validPlan));

        evaluationPlanService.activatePlan(1, "profesor@nur.edu.bo");

        assertEquals(RecordStatus.ACTIVE, mockSubject.getRecordStatus());
        verify(subjectRepository, times(1)).save(mockSubject);
    }

}
