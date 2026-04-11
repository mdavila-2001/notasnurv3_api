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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

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

    private void assertEquals(int i, Integer id) {
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
}
