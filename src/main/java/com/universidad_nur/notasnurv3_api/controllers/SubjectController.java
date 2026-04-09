package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.SubjectRequest;
import com.universidad_nur.notasnurv3_api.dto.SubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubjectController {

    private final SubjectService subjectService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponseDTO> create(@RequestBody SubjectRequest request) {
        SubjectResponseDTO newSubject = subjectService.createSubject(request);
        return new ResponseEntity<>(newSubject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAll() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }


    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponseDTO> update(@PathVariable Integer id, @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<SubjectResponseDTO>> activate(@PathVariable Integer id) {
        SubjectResponseDTO activatedSubject = subjectService.activateSubject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia activada exitosamente", activatedSubject));
    }
}