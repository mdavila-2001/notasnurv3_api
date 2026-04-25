package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-degrees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserDegreeController {
    private final UserDegreeService userDegreeService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<UserDegreeResponse>> openAcademicRecord(@Valid @RequestBody UserDegreeRequest request) {
        // En el servicio, Rodrigo debe setear el status como ACTIVE por defecto
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Expediente académico abierto", userDegreeService.openRecord(request)));
    }

    // Para que el frontend sepa qué expedientes tiene un alumno
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN) or hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_STUDENT)")
    public ResponseEntity<ApiResponse<List<UserDegreeResponse>>> getRecordsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Expedientes obtenidos", userDegreeService.getByUserId(userId)));
    }
}