package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.AttendanceBulkRequest;
import com.universidad_nur.notasnurv3_api.services.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<Void>> saveBulkAttendance(
            @Valid @RequestBody AttendanceBulkRequest request,
            Authentication authentication
    ) {
        attendanceService.saveBulkAttendance(request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia registrada correctamente", null));
    }
}
