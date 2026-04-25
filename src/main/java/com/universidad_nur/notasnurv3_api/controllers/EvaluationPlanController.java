package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.EvaluationPlanResponse;
import com.universidad_nur.notasnurv3_api.services.EvaluationPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<EvaluationPlanResponse>> getBySubject(
            @PathVariable Integer subjectId,
            Authentication authentication
    ) {
        EvaluationPlanResponse response = evaluationPlanService.getBySubject(subjectId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Plan obtenido", response));
    }

    @PostMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<EvaluationPlanResponse>> createForSubject(
            @PathVariable Integer subjectId,
            Authentication authentication
    ) {
        EvaluationPlanResponse response = evaluationPlanService.createForSubject(subjectId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Plan creado exitosamente", response));
    }

}
