package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ComponentRequest;
import com.universidad_nur.notasnurv3_api.dto.ComponentResponse;
import com.universidad_nur.notasnurv3_api.entities.Component;
import com.universidad_nur.notasnurv3_api.entities.EvaluationPlan;
import com.universidad_nur.notasnurv3_api.repositories.ComponentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComponentService {
    private final ComponentRepository componentRepository;
    private final EvaluationPlanRepository evaluationPlanRepository;

    @Transactional
    public ComponentResponse addComponent(ComponentRequest request) {
        EvaluationPlan plan = evaluationPlanRepository.findById(request.planId())
                .orElseThrow(() -> new RuntimeException("Plan de evaluación no encontrado."));

        Component component = Component.builder()
                .plan(plan)
                .name(request.name())
                .weight(request.weight())
                .description(request.description())
                .build();

        Component saved = componentRepository.save(component);
        return new ComponentResponse(saved.getId(), saved.getName(), saved.getWeight(), saved.getDescription());
    }
}
