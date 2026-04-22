package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
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

    @Transactional
    public void withdrawStudent(java.util.UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("La matrícula no existe o el alumno ya fue dado de baja."));

        Subject subject = enrollment.getSubject();
        
        // Sumar 1 al cupo
        subject.setCapacity(subject.getCapacity() + 1);
        subjectRepository.save(subject);

        // Dar de baja (Soft Delete)
        enrollmentRepository.delete(enrollment);
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

    public java.util.List<StudentResponseDTO> getStudentsBySubject(Integer subjectId, Users currentUser) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("La materia con ID " + subjectId + " no fue encontrada."));

        // Seguridad: Solo Admin o el Profesor de la materia pueden ver
        if (!currentUser.getRole().isAdmin()) {
            if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(currentUser.getId())) {
                throw new UnauthorizedAccessException("No tienes permisos para ver los alumnos de esta materia.");
            }
        }

        java.util.List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId);

        return enrollments.stream().map(enrollment -> StudentResponseDTO.builder()
                .studentId(enrollment.getStudent().getId())
                .fullName(enrollment.getStudent().getFullName())
                .ci(enrollment.getStudent().getCi())
                .email(enrollment.getStudent().getEmail())
                .build()
        ).toList();
    }

    public java.util.List<MySubjectResponseDTO> getMySubjects(Users currentUser) {
        java.util.List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUser.getId());

        return enrollments.stream().map(enrollment -> {
            Subject subject = enrollment.getSubject();
            String teacherName = subject.getTeacher() != null ? subject.getTeacher().getFullName() : "Sin asignar";
            
            return MySubjectResponseDTO.builder()
                    .subjectCode(subject.getCode())
                    .subjectName(subject.getName())
                    .teacherName(teacherName)
                    .build();
        }).toList();
    }
}
