package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.GradeBulkRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeResponse;
import com.universidad_nur.notasnurv3_api.services.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGradesBySubject(
            @PathVariable Integer subjectId,
            Authentication authentication
    ) {
        List<GradeResponse> grades = gradeService.getGradesBySubject(subjectId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Calificaciones obtenidas correctamente", grades));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<GradeResponse>> saveGrade(
            @Valid @RequestBody GradeRequest request,
            Authentication authentication
    ) {
        GradeResponse response = gradeService.saveGrade(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Calificación registrada correctamente", response));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> saveGrades(
            @Valid @RequestBody GradeBulkRequest request,
            Authentication authentication
    ) {
        List<GradeResponse> response = gradeService.bulkSaveGrades(request.grades(), authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Calificaciones registradas correctamente", response));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> bulkSaveGrades(
            @Valid @RequestBody List<GradeRequest> request,
            Authentication authentication
    ) {
        List<GradeResponse> response = gradeService.bulkSaveGrades(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Calificaciones masivas registradas correctamente", response));
    }
}
