package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.services.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Estudiante inscrito exitosamente", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> withdrawStudent(@PathVariable java.util.UUID id) {
        enrollmentService.withdrawStudent(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Estudiante dado de baja exitosamente", null));
    }

    @GetMapping("/subject/{subjectId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<java.util.List<StudentResponseDTO>>> getStudentsBySubject(
            @PathVariable Integer subjectId,
            Authentication authentication) {
        
        Users currentUser = (Users) authentication.getPrincipal();
        java.util.List<StudentResponseDTO> students = enrollmentService.getStudentsBySubject(subjectId, currentUser);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Alumnos obtenidos exitosamente", students));
    }

    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<java.util.List<MySubjectResponseDTO>>> getMySubjects(Authentication authentication) {
        Users currentUser = (Users) authentication.getPrincipal();
        java.util.List<MySubjectResponseDTO> subjects = enrollmentService.getMySubjects(currentUser);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Materias obtenidas exitosamente", subjects));
    }
}
