package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.SemesterRequestDTO;
import com.universidad_nur.notasnurv3_api.dto.SemesterResponseDTO;
import com.universidad_nur.notasnurv3_api.services.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponseDTO>> createSemester(@Valid @RequestBody SemesterRequestDTO request) {
        SemesterResponseDTO response = semesterService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Semestre creado correctamente", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SemesterResponseDTO>>> getAllSemesters() {
        List<SemesterResponseDTO> response = semesterService.getAllSemesters();
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestres obtenidos correctamente", response));
    }

    @GetMapping("/by-management/{managementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SemesterResponseDTO>>> getSemestersByManagement(@PathVariable Long managementId) {
        List<SemesterResponseDTO> response = semesterService.getSemestersByManagement(managementId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Semestres obtenidos correctamente", response));
    }
}