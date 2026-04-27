package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.services.FacultyService; // Corregido: Importamos el Service, no el Test
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacultyController {

    // Corregido: El tipo de dato debe ser el Service real
    private final FacultyService facultyService; 

    @GetMapping("/{facultyId}/stats")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyStatsResponse>> getFacultyStats(@PathVariable Integer facultyId) {
        
        // Llamamos al método getStats del FacultyService
        FacultyStatsResponse stats = facultyService.getStats(facultyId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Estadísticas obtenidas", stats));
    }
}