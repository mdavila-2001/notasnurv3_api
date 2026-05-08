package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ComponentResponse;
import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationPlanService {

    private final EvaluationPlanRepository evaluationPlanRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public EvaluationPlanResponse getBySubject(Integer subjectId, String teacherEmail) {
        validateSubjectAndTeacher(subjectId, teacherEmail);

        EvaluationPlan plan = evaluationPlanRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Esta materia aún no tiene un plan de evaluación configurado."));

        return toResponse(plan);
    }

    @Transactional
    public EvaluationPlanResponse createForSubject(Integer subjectId, String teacherEmail) {
        Subject subject = validateSubjectAndTeacher(subjectId, teacherEmail);

        Optional<EvaluationPlan> existingPlan = evaluationPlanRepository.findBySubjectId(subjectId);
        if (existingPlan.isPresent()) {
            return toResponse(existingPlan.get());
        }

        EvaluationPlan newPlan = evaluationPlanRepository.save(
                EvaluationPlan.builder().subject(subject).build()
        );
        return toResponse(newPlan);
    }

    @Transactional
    public void activatePlan(Integer subjectId, String teacherEmail) {
        Subject subject = validateSubjectAndTeacher(subjectId, teacherEmail);

        EvaluationPlan plan = evaluationPlanRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Esta materia aún no tiene un plan de evaluación configurado."));

        if (plan.getComponents() == null || plan.getComponents().isEmpty()) {
            throw new com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException("No se puede activar un plan de evaluación sin componentes.");
        }

        BigDecimal totalWeight = plan.getComponents().stream()
                .map(com.universidad_nur.notasnurv3_api.entities.Component::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException("La suma de las ponderaciones debe ser exactamente 100.");
        }

        subject.setRecordStatus(com.universidad_nur.notasnurv3_api.entities.RecordStatus.ACTIVE);
        subjectRepository.save(subject);
    }

    private Subject validateSubjectAndTeacher(Integer subjectId, String teacherEmail) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        if (subject.getTeacher() == null || !subject.getTeacher().getEmail().equalsIgnoreCase(teacherEmail)) {
            throw new UnauthorizedAccessException("No tienes permisos para acceder al plan de esta materia.");
        }
        return subject;
    }

    private EvaluationPlanResponse toResponse(EvaluationPlan plan) {
        return new EvaluationPlanResponse(
                plan.getId(),
                plan.getSubject().getId(),
                plan.getComponents() == null ? Collections.emptyList() :
                        plan.getComponents().stream()
                        .map(c -> new ComponentResponse(c.getId(), c.getName(), c.getWeight(), c.getDescription()))
                        .collect(Collectors.toList())
        );
    }
}
