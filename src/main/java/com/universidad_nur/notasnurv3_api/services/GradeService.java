package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.GradeRequest;
import com.universidad_nur.notasnurv3_api.dto.GradeResponse;
import com.universidad_nur.notasnurv3_api.entities.Component;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.Grade;
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

        Component component = componentRepository.findById(request.componentId())
                .orElseThrow(() -> new ResourceNotFoundException("Componente no encontrado."));

        // Validar que el componente pertenece a la materia en la que el estudiante está inscrito
        if (!component.getPlan().getSubject().getId().equals(enrollment.getSubject().getId())) {
            throw new InvalidOperationException("El componente no pertenece a la materia inscrita.");
        }

        if (enrollment.getSubject().getRecordStatus() == com.universidad_nur.notasnurv3_api.entities.RecordStatus.CLOSED) {
            throw new UnauthorizedAccessException("La materia se encuentra cerrada. No se pueden modificar calificaciones.");
        }

        // Validar que el docente que intenta poner la nota es el asignado a la materia
        if (enrollment.getSubject().getTeacher() == null || !enrollment.getSubject().getTeacher().getEmail().equalsIgnoreCase(teacherEmail)) {
            throw new UnauthorizedAccessException("No tienes permisos para registrar notas en esta materia.");
        }

        // Validar que la nota sea mayor o igual a cero
        if (request.score().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("La nota no puede ser negativa.");
        }

        // Validar que la nota no sobrepase el peso del componente
        if (request.score().compareTo(component.getWeight()) > 0) {
            throw new InvalidOperationException("La nota (" + request.score() + ") supera la ponderación del componente (" + component.getWeight() + ").");
        }

        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        Optional<Grade> existingGradeOpt = gradeRepository.findByEnrollmentIdAndComponentId(request.enrollmentId(), request.componentId());

        Grade grade;
        if (existingGradeOpt.isPresent()) {
            grade = existingGradeOpt.get();
            grade.setScore(request.score());
            grade.setTeacher(teacher);
        } else {
            grade = Grade.builder()
                    .enrollment(enrollment)
                    .component(component)
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
}
