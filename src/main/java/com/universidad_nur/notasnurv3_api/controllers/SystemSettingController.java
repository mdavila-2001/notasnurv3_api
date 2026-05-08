package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.entities.SystemSetting;
import com.universidad_nur.notasnurv3_api.services.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuraciones obtenidas", systemSettingService.getAllSettings()));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<SystemSetting>> updateSetting(
            @PathVariable String key,
            @RequestParam String value,
            @RequestParam(required = false) String description) {
        
        SystemSetting updated = systemSettingService.updateSetting(key, value, description);
        return ResponseEntity.ok(new ApiResponse<>(true, "Configuración actualizada", updated));
    }
}
