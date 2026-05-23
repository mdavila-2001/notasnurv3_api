package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.AttendanceBulkRequest;
import com.universidad_nur.notasnurv3_api.dto.AttendanceAbsencesResponse;
import com.universidad_nur.notasnurv3_api.dto.AttendanceRecordResponse;
import com.universidad_nur.notasnurv3_api.services.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/subject/{subjectId}/absences")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<AttendanceAbsencesResponse>> getSubjectAbsences(
            @PathVariable Integer subjectId,
            Authentication authentication
    ) {
        AttendanceAbsencesResponse response = attendanceService.getSubjectAbsences(subjectId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Faltas por materia obtenidas correctamente", response));
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_TEACHER)")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getAttendanceBySubjectAndDate(
            @PathVariable Integer subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        List<AttendanceRecordResponse> response = attendanceService.getAttendanceBySubjectAndDate(
                subjectId,
                date,
                authentication.getName()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Registros de asistencia obtenidos correctamente", response));
    }
}
