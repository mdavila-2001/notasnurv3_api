package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementRequestDTO;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponseDTO;
import com.universidad_nur.notasnurv3_api.services.ManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/managements")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ManagementResponseDTO>> createManagement(@Valid @RequestBody ManagementRequestDTO request) {
        ManagementResponseDTO response = managementService.createManagement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gestión creada correctamente", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ManagementResponseDTO>>> getAllManagements() {
        List<ManagementResponseDTO> response = managementService.getAllManagements();
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestiones obtenidas correctamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteManagement(@PathVariable Long id) {
        managementService.deleteManagement(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión eliminada correctamente", null));
    }
}