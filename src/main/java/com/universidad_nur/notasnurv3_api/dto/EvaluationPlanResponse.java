package com.universidad_nur.notasnurv3_api.dto;

import java.util.List;

public record EvaluationPlanResponse(
        Integer id,
        Integer subjectId,
        List<ComponentResponse> components
) {
}
