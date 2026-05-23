package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.EvaluationPlanRepository;
import com.universidad_nur.notasnurv3_api.repositories.GradeRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {

    private final SubjectRepository subjectRepository;
    private final EvaluationPlanRepository evaluationPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final SystemSettingService systemSettingService;

    @Transactional
    public void calculateFinalGradesForSubject(Integer subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Materia no encontrada.");
        }

        EvaluationPlan plan = evaluationPlanRepository.findBySubjectId(subjectId).orElse(null);
        if (plan == null || plan.getComponents() == null || plan.getComponents().isEmpty()) {
            throw new InvalidOperationException("La materia no tiene un plan de evaluación asignado o componentes.");
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        for (Enrollment enrollment : enrollments) {
            // Reutilizar la colección grades ya precargada en memoria vía JOIN FETCH
            List<Grade> grades = enrollment.getGrades();

            BigDecimal sum = BigDecimal.ZERO;
            for (Components component : plan.getComponents()) {
                // Si el alumno tiene nota para este componente, se suma; de lo contrario, se asume 0
                BigDecimal score = grades.stream()
                        .filter(g -> g.getComponent().getId().equals(component.getId()))
                        .map(Grade::getScore)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                sum = sum.add(score);
            }

            // Aplicar redondeo NUR configurable
            RoundingMode roundingMode = systemSettingService.getRoundingMode();
            BigDecimal roundedScore = sum.setScale(0, roundingMode);
            int finalScore = roundedScore.intValue();

            enrollment.setFinalScore(finalScore);

            // Si el estado es FAILED_BY_ATTENDANCE, se respeta la reprobación.
            if (enrollment.getStatus() != EnrollmentStatus.FAILED_BY_ATTENDANCE) {
                if (finalScore >= 51) {
                    enrollment.setStatus(EnrollmentStatus.PASSED);
                } else {
                    enrollment.setStatus(EnrollmentStatus.FAILED);
                }
            }
        }

        // Guardar todos en masa fuera del bucle para evitar sobrecarga en la DB
        enrollmentRepository.saveAll(enrollments);

        log.info("Cálculo de notas finales completado para la materia ID: {}", subjectId);
    }
}
