package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.KardexResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.services.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Estudiante inscrito correctamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.universidad_nur.notasnurv3_api.config.SecurityAuthorities).ROLE_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> withdrawStudent(@PathVariable UUID id) {
        enrollmentService.withdrawStudent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Matrícula dada de baja correctamente", null));
    }

    @GetMapping("/subjects/{subjectId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> getStudentsBySubject(
            @PathVariable Integer subjectId,
            @AuthenticationPrincipal Users currentUser) {
        List<StudentResponseDTO> students = enrollmentService.getStudentsBySubject(subjectId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Alumnos obtenidos exitosamente", students));
    }

    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<MySubjectResponseDTO>>> getMySubjects(@AuthenticationPrincipal Users currentUser) {
        List<MySubjectResponseDTO> mySubjects = enrollmentService.getMySubjects(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materias obtenidas exitosamente", mySubjects));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<KardexResponse>> getMyKardex(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(new ApiResponse<>(true, "Kardex obtenido exitosamente", enrollmentService.getMyKardex(email)));
    }
}
