package com.universidad_nur.notasnurv3_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.GradeRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeResponse;
import com.universidad_nur.notasnurv3_api.services.GradeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<GradeResponse>> saveGrade(
            @Valid @RequestBody GradeRequest request,
            Authentication authentication
    ) {
        GradeResponse response = gradeService.saveGrade(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Calificación registrada correctamente", response));
    }
}