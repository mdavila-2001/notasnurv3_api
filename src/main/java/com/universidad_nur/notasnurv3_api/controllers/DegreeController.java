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

    /**
     * GET /api/degrees - Listar todas las carreras
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getAllDegrees() {
        List<DegreeResponse> degrees = degreeService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Carreras obtenidas exitosamente", degrees));
    }

    /**
     * GET /api/degrees/{id} - Obtener una carrera por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> getDegreeById(@PathVariable Integer id) {
        DegreeResponse degree = degreeService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera obtenida exitosamente", degree));
    }

    /**
     * POST /api/degrees - Crear una nueva carrera
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> createDegree(@Valid @RequestBody DegreeRequest request) {
        DegreeResponse degree = degreeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Carrera creada exitosamente", degree));
    }

    /**
     * PUT /api/degrees/{id} - Actualizar una carrera existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> updateDegree(
            @PathVariable Integer id,
            @Valid @RequestBody DegreeRequest request) {
        DegreeResponse degree = degreeService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera actualizada exitosamente", degree));
    }

    /**
     * DELETE /api/degrees/{id} - Eliminar una carrera
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteDegree(@PathVariable Integer id) {
        degreeService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Carrera eliminada exitosamente", null));
    }
}
