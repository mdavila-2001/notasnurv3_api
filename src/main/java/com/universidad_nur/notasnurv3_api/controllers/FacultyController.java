package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.services.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacultyController {
    private final FacultyService facultyService;

    @GetMapping("/{facultyId}/stats")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyStatsResponse>> getFacultyStats(@PathVariable Integer facultyId) {

        FacultyStatsResponse stats = facultyService.getStats(facultyId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Estadísticas obtenidas", stats));
    }
}
