package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyRequest;
import com.universidad_nur.notasnurv3_api.dto.FacultyResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.services.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getAllFaculties() {
        List<FacultyResponse> faculties = facultyService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Facultades obtenidas exitosamente", faculties));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable Integer id) {
        FacultyResponse faculty = facultyService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facultad obtenida exitosamente", faculty));
    }

    @GetMapping("/{facultyId}/stats")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyStatsResponse>> getFacultyStats(@PathVariable Integer facultyId) {
        FacultyStatsResponse stats = facultyService.getStats(facultyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estadísticas obtenidas", stats));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyResponse>> createFaculty(@Valid @RequestBody FacultyRequest request) {
        FacultyResponse faculty = facultyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Facultad creada exitosamente", faculty));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
            @PathVariable Integer id,
            @Valid @RequestBody FacultyRequest request) {
        FacultyResponse faculty = facultyService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facultad actualizada exitosamente", faculty));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Integer id) {
        facultyService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Facultad eliminada exitosamente", null));
    }
}