package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ComponentResponse;
import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationPlanService {

    private final EvaluationPlanRepository evaluationPlanRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public EvaluationPlanResponse getOrCreateBySubject(Integer subjectId, String teacherEmail) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada."));

        if (subject.getTeacher() == null || !subject.getTeacher().getEmail().equalsIgnoreCase(teacherEmail)) {
            throw new RuntimeException("No tienes permisos para acceder al plan de esta materia.");
        }

        EvaluationPlan plan = evaluationPlanRepository.findBySubjectId(subjectId)
                .orElseGet(() -> evaluationPlanRepository.save(
                        EvaluationPlan.builder()
                                .subject(subject)
                                .build()
                ));

        return new EvaluationPlanResponse(
                plan.getId(),
                plan.getSubject().getId(),
                plan.getComponents() == null
                        ? Collections.emptyList()
                        : plan.getComponents().stream()
                                .map(component -> new ComponentResponse(
                                        component.getId(),
                                        component.getName(),
                                        component.getWeight(),
                                        component.getDescription()
                                ))
                                .collect(Collectors.toList())
        );
    }
}
