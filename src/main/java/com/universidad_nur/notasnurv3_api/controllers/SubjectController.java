package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.SubjectRequest;
import com.universidad_nur.notasnurv3_api.entities.Subject;
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
    public ResponseEntity<Subject> create(@RequestBody SubjectRequest request) {
        Subject newSubject = subjectService.createSubject(request);
        return new ResponseEntity<>(newSubject, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Subject>> getAll() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Subject> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Subject> update(@PathVariable Integer id, @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Integer id) {
        subjectService.activateSubject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia activada y lista para recibir inscripciones", null));
    }
}