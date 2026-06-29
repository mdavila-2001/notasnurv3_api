package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.SemesterRequest;
import com.universidad_nur.notasnurv3_api.dto.SemesterResponse;
import com.universidad_nur.notasnurv3_api.services.SemesterService;
import com.universidad_nur.notasnurv3_api.services.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;
    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Semestre creado correctamente", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getAllSemesters() {
        List<SemesterResponse> response = semesterService.getAllSemesters();
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestres obtenidos correctamente", response));
    }

    @GetMapping("/by-management/{managementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getSemestersByManagement(@PathVariable Integer managementId) {
        List<SemesterResponse> response = semesterService.getSemestersByManagement(managementId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestres obtenidos correctamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestre encontrado", semesterService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestre actualizado", semesterService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        semesterService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestre eliminado correctamente", null));
    }

    @PutMapping("/{id}/close-subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> closeSubjectsBySemester(@PathVariable Integer id) {
        subjectService.closeSubjectsBySemester(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cierre masivo de materias procesado.", null));
    }
}