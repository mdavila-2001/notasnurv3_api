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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {

    private final SubjectRepository subjectRepository;
    private final EvaluationPlanRepository evaluationPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;

    @Transactional
    public void calculateFinalGradesForSubject(Integer subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        EvaluationPlan plan = evaluationPlanRepository.findBySubjectId(subjectId).orElse(null);
        if (plan == null || plan.getComponents() == null || plan.getComponents().isEmpty()) {
            throw new InvalidOperationException("La materia no tiene un plan de evaluación asignado o componentes.");
        }

        int requiredGradesCount = plan.getComponents().size();

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeRepository.findByEnrollmentId(enrollment.getId());

            if (grades.size() < requiredGradesCount) {
                throw new InvalidOperationException("Faltan notas para el alumno con inscripción ID: " + enrollment.getId() + ". No se puede procesar la nota final.");
            }

            BigDecimal sum = BigDecimal.ZERO;
            for (Grade grade : grades) {
                sum = sum.add(grade.getScore());
            }

            long finalScoreLong = Math.round(sum.doubleValue());
            int finalScore = (int) finalScoreLong;

            enrollment.setFinalScore(finalScore);

            // Si el estado es FAILED_BY_ATTENDANCE, se respeta la reprobación.
            if (enrollment.getStatus() != EnrollmentStatus.FAILED_BY_ATTENDANCE) {
                if (finalScore >= 51) {
                    enrollment.setStatus(EnrollmentStatus.PASSED);
                } else {
                    enrollment.setStatus(EnrollmentStatus.FAILED);
                }
            }

            enrollmentRepository.save(enrollment);
        }

        log.info("Cálculo de notas finales completado para la materia ID: {}", subjectId);
    }
}
