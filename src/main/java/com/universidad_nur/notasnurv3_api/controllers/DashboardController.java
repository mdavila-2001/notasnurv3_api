package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardAdminDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardStudentDTO;
import com.universidad_nur.notasnurv3_api.dto.dashboard.DashboardTeacherDTO;
import com.universidad_nur.notasnurv3_api.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<DashboardAdminDTO>> getAdminDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen administrativo obtenido", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<DashboardTeacherDTO>> getTeacherDashboard(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen de docente obtenido", dashboardService.getTeacherDashboard(email)));
    }

    @GetMapping("/student")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_STUDENT)")
    public ResponseEntity<ApiResponse<DashboardStudentDTO>> getStudentDashboard(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen de estudiante obtenido", dashboardService.getStudentDashboard(email)));
    }
}
