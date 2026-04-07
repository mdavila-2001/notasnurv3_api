package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementRequest;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponse;
import com.universidad_nur.notasnurv3_api.services.ManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managements")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ManagementResponse>> createManagement(@Valid @RequestBody ManagementRequest request) {
        ManagementResponse response = managementService.createManagement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gestión creada correctamente", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ManagementResponse>>> getAllManagements() {
        List<ManagementResponse> response = managementService.getAllManagements();
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestiones obtenidas correctamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagementResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión encontrada", managementService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagementResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ManagementRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión actualizada", managementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteManagement(@PathVariable Long id) {
        managementService.deleteManagement(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión eliminada correctamente", null));
    }
}