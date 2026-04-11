package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.services.EvaluationPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EvaluationPlanController {

    private final EvaluationPlanService evaluationPlanService;

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<EvaluationPlanResponse>> getOrCreateBySubject(
            @PathVariable Integer subjectId,
            Authentication authentication
    ) {
        EvaluationPlanResponse response = evaluationPlanService.getOrCreateBySubject(
                subjectId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Plan de evaluación obtenido correctamente", response)
        );
    }
}
