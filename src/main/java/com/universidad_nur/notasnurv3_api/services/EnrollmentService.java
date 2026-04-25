package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.*;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final UserDegreeRepository userDegreeRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        // 1. Verificar Estudiante
        UserDegree academicRecord = userDegreeRepository.findById(request.userDegreeId())
                .orElseThrow(() -> new RuntimeException("El expediente académico con ID " + request.userDegreeId() + " no fue encontrado."));

        if (academicRecord.getStatus() != AcademicStatus.ACTIVE) {
            throw new RuntimeException("No se puede inscribir: El expediente del alumno no se encuentra ACTIVO en esta carrera.");
        }

        // 2. Verificar Materia
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new RuntimeException("La materia con ID " + request.subjectId() + " no fue encontrada."));

        if (subject.getRecordStatus() == RecordStatus.DRAFT) {
            throw new RuntimeException("La materia está en estado DRAFT (Borrador) y no admite inscripciones.");
        }

        if (subject.getCapacity() <= 0) {
            throw new RuntimeException("No hay cupos disponibles para esta materia.");
        }

        if (enrollmentRepository.existsByAcademicRecordIdAndSubjectId(academicRecord.getId(), subject.getId())) {
            throw new DuplicateResourceException("El estudiante ya se encuentra inscrito en esta materia bajo este expediente.");
        }

        subject.setCapacity(subject.getCapacity() - 1);
        subjectRepository.save(subject);

        // Crear e inscribir
        Enrollment enrollment = Enrollment.builder()
                .academicRecord(academicRecord) // <- Cambio crítico
                .subject(subject)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return EnrollmentResponse.builder()
                .id(savedEnrollment.getId())
                .studentName(academicRecord.getUser().getFullName())
                .studentCi(academicRecord.getUser().getCi())
                .subjectCode(subject.getCode())
                .subjectName(subject.getName())
                .enrolledAt(savedEnrollment.getCreatedAt())
                .build();
    }

    @Transactional
    public void withdrawStudent(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("La matrícula no existe o el alumno ya fue dado de baja."));

        if (enrollment.getStatus() == EnrollmentStatus.WITHDRAWN) {
            return;
        }

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new RuntimeException("La matrícula no está activa o el alumno ya fue dado de baja.");
        }

        Subject subject = enrollment.getSubject();
        
        // Sumarle 1 al cupo
        subject.setCapacity(subject.getCapacity() + 1);
        subjectRepository.save(subject);

        // Dar de baja (Cambio de estado en lugar de Soft Delete)
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponse mapToResponseDTO(Enrollment enrollment) {
        Users student = enrollment.getAcademicRecord().getUser();
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentName(student.getFullName())
                .studentCi(student.getCi())
                .subjectCode(enrollment.getSubject().getCode())
                .subjectName(enrollment.getSubject().getName())
                .enrolledAt(enrollment.getCreatedAt())
                .build();
    }

    public java.util.List<StudentResponseDTO> getStudentsBySubject(Integer subjectId, Users currentUser) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("La materia con ID " + subjectId + " no fue encontrada."));

        // Seguridad: Solo Admin o el Profesor de la materia pueden ver
        if (!currentUser.getRole().isAdmin()) {
            if (subject.getTeacher() == null || !subject.getTeacher().getId().equals(currentUser.getId())) {
                throw new UnauthorizedAccessException("No tienes permisos para ver los alumnos de esta materia.");
            }
        }

        java.util.List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndStatus(subjectId, EnrollmentStatus.ACTIVE);

        return enrollments.stream().map(enrollment -> {
            Users student = enrollment.getAcademicRecord().getUser();
            return StudentResponseDTO.builder()
                    .studentId(student.getId())
                    .fullName(student.getFullName())
                    .ci(student.getCi())
                    .email(student.getEmail())
                    .build();
        }).toList();
    }

    public java.util.List<MySubjectResponseDTO> getMySubjects(Users currentUser) {
        java.util.List<Enrollment> enrollments = enrollmentRepository.findByAcademicRecord_UserIdAndStatus(currentUser.getId(), EnrollmentStatus.ACTIVE);

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
