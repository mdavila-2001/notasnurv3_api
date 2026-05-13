package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.GradeRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeResponse;
import com.universidad_nur.notasnurv3_api.entities.Components;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.Grade;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.ComponentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.GradeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;

    @Transactional
    public GradeResponse saveGrade(GradeRequest request, String teacherEmail) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada."));

        Subject subject = enrollment.getSubject();

        if (subject.getRecordStatus() == RecordStatus.CLOSED) {
            throw new UnauthorizedAccessException("La materia se encuentra cerrada. No se pueden modificar calificaciones.");
        }

        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("No tienes permisos para registrar notas en esta materia.");
        }

        Components components = componentRepository.findById(request.componentId())
                .orElseThrow(() -> new ResourceNotFoundException("Componente no encontrado."));

        // Validar que el componente pertenece a la materia en la que el estudiante está inscrito
        if (!components.getPlan().getSubject().getId().equals(subject.getId())) {
            throw new InvalidOperationException("El componente no pertenece a la materia inscrita.");
        }

        // Validar que la nota sea mayor o igual a cero
        if (request.score().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("La nota no puede ser negativa.");
        }

        // Validar que la nota no sobrepase el peso del componente
        if (request.score().compareTo(components.getWeight()) > 0) {
            throw new InvalidOperationException("La nota (" + request.score() + ") supera la ponderación del componente (" + components.getWeight() + ").");
        }

        Optional<Grade> existingGradeOpt = gradeRepository.findByEnrollmentIdAndComponents_Id(request.enrollmentId(), request.componentId());

        Grade grade;
        if (existingGradeOpt.isPresent()) {
            grade = existingGradeOpt.get();
            grade.setScore(request.score());
            grade.setTeacher(teacher);
        } else {
            grade = Grade.builder()
                    .enrollment(enrollment)
                    .component(components)
                    .teacher(teacher)
                    .score(request.score())
                    .build();
        }

        Grade saved = gradeRepository.save(grade);

        return new GradeResponse(
                saved.getId(),
                saved.getEnrollment().getId(),
                saved.getComponent().getId(),
                saved.getTeacher().getId(),
                saved.getScore()
        );
    }

    @Transactional
    public List<GradeResponse> bulkSaveGrades(List<GradeRequest> gradeRequests, String teacherEmail) {
        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        // Validar todas las solicitudes antes de guardar
        List<Grade> gradesToSave = gradeRequests.stream()
                .map(request -> validateAndCreateGrade(request, teacher))
                .toList();

        // Guardar todas las notas en una sola transacción
        List<Grade> savedGrades = gradeRepository.bulkSave(gradesToSave);

        return savedGrades.stream()
                .map(grade -> new GradeResponse(
                        grade.getId(),
                        grade.getEnrollment().getId(),
                        grade.getComponent().getId(),
                        grade.getTeacher().getId(),
                        grade.getScore()
                ))
                .toList();
    }

    private Grade validateAndCreateGrade(GradeRequest request, Users teacher) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada."));

        Subject subject = enrollment.getSubject();

        if (subject.getRecordStatus() == RecordStatus.CLOSED) {
            throw new UnauthorizedAccessException("La materia se encuentra cerrada. No se pueden modificar calificaciones.");
        }

        if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(teacher.getId())) {
            throw new UnauthorizedAccessException("No tienes permisos para registrar notas en esta materia.");
        }

        Components components = componentRepository.findById(request.componentId())
                .orElseThrow(() -> new ResourceNotFoundException("Componente no encontrado."));

        // Validar que el componente pertenece a la materia en la que el estudiante está inscrito
        if (!components.getPlan().getSubject().getId().equals(subject.getId())) {
            throw new InvalidOperationException("El componente no pertenece a la materia inscrita.");
        }

        // Validar que la nota sea mayor o igual a cero
        if (request.score().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("La nota no puede ser negativa.");
        }

        // Validar que la nota no sobrepase el peso del componente
        if (request.score().compareTo(components.getWeight()) > 0) {
            throw new InvalidOperationException("La nota (" + request.score() + ") supera la ponderación del componente (" + components.getWeight() + ").");
        }

        Optional<Grade> existingGradeOpt = gradeRepository.findByEnrollmentIdAndComponents_Id(request.enrollmentId(), request.componentId());

        if (existingGradeOpt.isPresent()) {
            Grade existingGrade = existingGradeOpt.get();
            existingGrade.setScore(request.score());
            existingGrade.setTeacher(teacher);
            return existingGrade;
        } else {
            return Grade.builder()
                    .enrollment(enrollment)
                    .component(components)
                    .teacher(teacher)
                    .score(request.score())
                    .build();
        }
    }
}
