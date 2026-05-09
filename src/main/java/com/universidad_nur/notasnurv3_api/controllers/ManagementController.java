package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementRequest;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponse;
import com.universidad_nur.notasnurv3_api.dto.ManagementStatsResponse;
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
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<ManagementResponse>> createManagement(@Valid @RequestBody ManagementRequest request) {
        ManagementResponse response = managementService.createManagement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Gestión creada correctamente", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<List<ManagementResponse>>> getAllManagements() {
        List<ManagementResponse> response = managementService.getAllManagements();
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestiones obtenidas correctamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagementResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión encontrada", managementService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManagementResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ManagementRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión actualizada", managementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteManagement(@PathVariable Integer id) {
        managementService.deleteManagement(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Gestión eliminada correctamente", null));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<ManagementStatsResponse>> getStats(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estadísticas obtenidas correctamente", managementService.getStats(id)));
    }
}