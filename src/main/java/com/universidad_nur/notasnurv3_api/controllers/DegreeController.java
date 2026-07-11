package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.DegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.DegreeResponse;
import com.universidad_nur.notasnurv3_api.services.DegreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/degrees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DegreeController {

    private final DegreeService degreeService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getAllDegrees() {
        List<DegreeResponse> degrees = degreeService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Carreras obtenidas exitosamente", degrees));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> getDegreeById(@PathVariable Integer id) {
        DegreeResponse degree = degreeService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera obtenida exitosamente", degree));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> createDegree(@Valid @RequestBody DegreeRequest request) {
        DegreeResponse degree = degreeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Carrera creada exitosamente", degree));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> updateDegree(
            @PathVariable Integer id,
            @Valid @RequestBody DegreeRequest request) {
        DegreeResponse degree = degreeService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera actualizada exitosamente", degree));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteDegree(@PathVariable Integer id) {
        degreeService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera eliminada exitosamente", null));
    }
}
