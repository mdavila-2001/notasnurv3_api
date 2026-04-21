package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        // 1. Verificar Estudiante
        Users student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("El estudiante con ID " + request.getStudentId() + " no fue encontrado."));

        if (!student.getRole().isStudent()) {
            throw new RuntimeException("El usuario seleccionado no es un estudiante válido.");
        }

        // 2. Verificar Materia
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("La materia con ID " + request.getSubjectId() + " no fue encontrada."));

        // Regla 1: Materia no en Draft
        if (subject.getRecordStatus() == RecordStatus.DRAFT) {
            throw new RuntimeException("La materia está en estado DRAFT (Borrador) y no admite inscripciones.");
        }

        // Regla 2: Cupo disponible
        if (subject.getCapacity() <= 0) {
            throw new RuntimeException("No hay cupos disponibles para esta materia.");
        }

        // Regla 3: Duplicidad
        if (enrollmentRepository.existsByStudentIdAndSubjectId(student.getId(), subject.getId())) {
            throw new DuplicateResourceException("El estudiante ya se encuentra inscrito en esta materia.");
        }

        // Regla 4: Reducir cupo
        subject.setCapacity(subject.getCapacity() - 1);
        subjectRepository.save(subject);

        // Crear e incribir
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .subject(subject)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return mapToResponseDTO(savedEnrollment);
    }

    private EnrollmentResponse mapToResponseDTO(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentName(enrollment.getStudent().getFullName())
                .studentCi(enrollment.getStudent().getCi())
                .subjectCode(enrollment.getSubject().getCode())
                .subjectName(enrollment.getSubject().getName())
                .enrolledAt(enrollment.getCreatedAt())
                .build();
    }
}
