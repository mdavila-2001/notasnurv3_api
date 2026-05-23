package com.universidad_nur.notasnurv3_api.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.universidad_nur.notasnurv3_api.entities.AuditLog;
import com.universidad_nur.notasnurv3_api.repositories.AuditLogRepository;
import com.universidad_nur.notasnurv3_api.repositories.ComponentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.GradeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;
    private final SystemSettingService systemSettingService;
    private final AuditLogRepository auditLogRepository;

    /**
     * Obtiene todas las calificaciones de todos los estudiantes de una materia.
     * Valida que el docente autenticado sea el titular de la materia.
     * Reutiliza EnrollmentRepository.findBySubjectId que ya carga grades via JOIN FETCH.
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesBySubject(Integer subjectId, String teacherEmail) {
        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        // Validar que el docente sea el titular de al menos una de las inscripciones
        if (!enrollments.isEmpty()) {
            Subject subject = enrollments.get(0).getSubject();
            if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(teacher.getId())) {
                throw new UnauthorizedAccessException("No tienes permisos para ver las notas de esta materia.");
            }
        }

        return enrollments.stream()
                .flatMap(enrollment -> enrollment.getGrades().stream()
                        .map(grade -> new GradeResponse(
                                grade.getId(),
                                enrollment.getId(),
                                grade.getComponent().getId(),
                                grade.getTeacher() != null ? grade.getTeacher().getId() : null,
                                grade.getScore()
                        ))
                )
                .toList();
    }

    @Transactional
    public GradeResponse saveGrade(GradeRequest request, String teacherEmail) {
        Users teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado."));

        boolean isNew = gradeRepository.findByEnrollmentIdAndComponent_Id(request.enrollmentId(), request.componentId()).isEmpty();

        Grade grade = validateAndPrepareGrade(request, teacher);
        Grade saved = gradeRepository.save(grade);

        if (isNew) {
            auditLogRepository.save(AuditLog.builder()
                    .entityName("GRADE")
                    .entityRefId(saved.getId().toString())
                    .actionType("CREATE")
                    .oldValue("N/A")
                    .newValue(saved.getScore().toString())
                    .changedBy(teacher.getEmail())
                    .build());
        }

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

        List<Grade> gradesToSave = new java.util.ArrayList<>();
        List<Grade> newGradesAfterSave = new java.util.ArrayList<>();

        for (GradeRequest request : gradeRequests) {
            boolean isNew = gradeRepository.findByEnrollmentIdAndComponent_Id(request.enrollmentId(), request.componentId()).isEmpty();
            Grade grade = validateAndPrepareGrade(request, teacher);
            gradesToSave.add(grade);
            if (isNew) {
                newGradesAfterSave.add(grade);
            }
        }

        List<Grade> savedGrades = gradeRepository.bulkSave(gradesToSave);

        for (Grade saved : savedGrades) {
            boolean wasNew = newGradesAfterSave.stream()
                    .anyMatch(g -> g.getEnrollment().getId().equals(saved.getEnrollment().getId()) 
                            && g.getComponent().getId().equals(saved.getComponent().getId()));
            if (wasNew) {
                auditLogRepository.save(AuditLog.builder()
                        .entityName("GRADE")
                        .entityRefId(saved.getId().toString())
                        .actionType("CREATE")
                        .oldValue("N/A")
                        .newValue(saved.getScore().toString())
                        .changedBy(teacher.getEmail())
                        .build());
            }
        }

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

    private Grade validateAndPrepareGrade(GradeRequest request, Users teacher) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada."));

        Subject subject = enrollment.getSubject();

        // Validar límite temporal de registro/modificación de notas
        int graceDays = systemSettingService.getIntValue("GRADING_GRACE_DAYS", 0);
        LocalDate deadline = subject.getSemester().getEndDate().plusDays(graceDays);
        if (LocalDate.now().isAfter(deadline)) {
            throw new InvalidOperationException("Se ha superado la fecha límite establecida para la carga de notas.");
        }

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

        Optional<Grade> existingGradeOpt = gradeRepository.findByEnrollmentIdAndComponent_Id(request.enrollmentId(), request.componentId());

        if (existingGradeOpt.isPresent()) {
            Grade existingGrade = existingGradeOpt.get();
            BigDecimal oldScore = existingGrade.getScore();
            if (oldScore.compareTo(request.score()) != 0) {
                auditLogRepository.save(AuditLog.builder()
                        .entityName("GRADE")
                        .entityRefId(existingGrade.getId().toString())
                        .actionType("UPDATE")
                        .oldValue(oldScore.toString())
                        .newValue(request.score().toString())
                        .changedBy(teacher.getEmail())
                        .build());
            }
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

