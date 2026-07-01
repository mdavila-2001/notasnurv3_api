package com.universidad_nur.notasnurv3_api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.GlobalSettingsResponse;
import com.universidad_nur.notasnurv3_api.entities.SystemSetting;
import com.universidad_nur.notasnurv3_api.services.SystemSettingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuraciones obtenidas", systemSettingService.getAllSettings()));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemSetting>> updateSetting(
            @PathVariable String key,
            @RequestParam String value,
            @RequestParam(required = false) String description) {
        
        SystemSetting updated = systemSettingService.updateSetting(key, value, description);
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuración actualizada", updated));
    }

    // ========================================================================
    // ENDPOINTS GLOBALES (US-13) - PATRÓN FACADE
    // ========================================================================

    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<GlobalSettingsResponse>> getGlobalSettings() {
        GlobalSettingsResponse response = systemSettingService.getGlobalSettings();
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuración global obtenida", response));
    }

    // Aceptamos POST y PUT aquí para evitar el conflicto
    @RequestMapping(value = "/global", method = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GlobalSettingsResponse>> updateGlobalSettings(@RequestBody GlobalSettingsResponse request) {
        GlobalSettingsResponse response = systemSettingService.saveGlobalSettings(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuración global actualizada correctamente", response));
    }
}