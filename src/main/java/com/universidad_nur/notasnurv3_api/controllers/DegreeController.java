package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.DegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.DegreeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.universidad_nur.notasnurv3_api.config.SecurityAuthorities;

import java.util.List;

@RestController
@RequestMapping("/api/degrees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DegreeController {
    private final DegreeService degreeService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DegreeResponse>> createDegree(@Valid @RequestBody DegreeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Carrera creada", degreeService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getAllDegrees() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Carreras obtenidas", degreeService.getAll()));
    }
}
