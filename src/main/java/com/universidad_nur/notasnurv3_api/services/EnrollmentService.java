package com.universidad_nur.notasnurv3_api.services;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.EnrollmentRequest;
import com.universidad_nur.notasnurv3_api.dto.EnrollmentResponse;
import com.universidad_nur.notasnurv3_api.dto.KardexResponse;
import com.universidad_nur.notasnurv3_api.dto.KardexResponse.SubjectGradeResponse;
import com.universidad_nur.notasnurv3_api.dto.MySubjectResponseDTO;
import com.universidad_nur.notasnurv3_api.dto.StudentResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Enrollment;
import com.universidad_nur.notasnurv3_api.entities.EnrollmentStatus;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.exceptions.UnauthorizedAccessException;
import com.universidad_nur.notasnurv3_api.repositories.EnrollmentRepository;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final UserDegreeRepository userDegreeRepository;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        // 1. Verificar Estudiante (UserDegree es el expediente académico)
        UserDegree academicRecord = userDegreeRepository.findById(request.userDegreeId())
                .orElseThrow(() -> new ResourceNotFoundException("El expediente académico con ID " + request.userDegreeId() + " no fue encontrado."));

        if (academicRecord.getStatus() != AcademicStatus.ACTIVE) {
            throw new InvalidOperationException("No se puede inscribir: El expediente del alumno no se encuentra ACTIVO en esta carrera.");
        }

        // 2. Verificar Materia
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("La materia con ID " + request.subjectId() + " no fue encontrada."));

        if (subject.getRecordStatus() == RecordStatus.DRAFT) {
            throw new InvalidOperationException("La materia está en estado DRAFT (Borrador) y no admite inscripciones.");
        }

        if (subject.getCapacity() <= 0) {
            throw new InvalidOperationException("No hay cupos disponibles para esta materia.");
        }

        if (enrollmentRepository.existsByAcademicRecordIdAndSubjectId(academicRecord.getId(), subject.getId())) {
            throw new DuplicateResourceException("El estudiante ya se encuentra inscrito en esta materia bajo este expediente.");
        }

        subject.setCapacity(subject.getCapacity() - 1);
        try {
            subjectRepository.saveAndFlush(subject);
        } catch (OptimisticLockingFailureException ex) {
            throw new OptimisticLockingFailureException(
                    "No se pudo completar la inscripción por una modificación concurrente en los cupos. Intente nuevamente.", ex);
        }

        // Crear e inscribir
        Enrollment enrollment = Enrollment.builder()
                .academicRecord(academicRecord)
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
                .orElseThrow(() -> new ResourceNotFoundException("La matrícula no existe o el alumno ya fue dado de baja."));

        if (enrollment.getStatus() == EnrollmentStatus.WITHDRAWN) {
            return;
        }

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new InvalidOperationException("La matrícula no está activa o el alumno ya fue dado de baja.");
        }

        Subject subject = enrollment.getSubject();
        subject.setCapacity(subject.getCapacity() + 1);
        try {
            subjectRepository.saveAndFlush(subject);
        } catch (OptimisticLockingFailureException ex) {
            throw new OptimisticLockingFailureException(
                    "No se pudo completar la baja por una modificación concurrente en los cupos. Intente nuevamente.", ex);
        }

        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollmentRepository.save(enrollment);
    }

    /**
     * Vista del Docente: Obtiene alumnos inscritos incluyendo el nombre de su carrera.
     */
    public java.util.List<StudentResponseDTO> getStudentsBySubject(Integer subjectId, Users currentUser) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("La materia con ID " + subjectId + " no fue encontrada."));

        // Seguridad: Solo Admin o el Profesor de la materia pueden ver los datos
        if (!currentUser.getRole().isAdmin() && (subject.getTeacher() == null || !subject.getTeacher().getId().equals(currentUser.getId()))) {
            throw new UnauthorizedAccessException("No tienes permisos para ver los alumnos de esta materia.");
        }

        java.util.List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(subjectId).stream()
            .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
            .toList();

        return enrollments.stream().map(enrollment -> {
            UserDegree academicRecord = enrollment.getAcademicRecord();
            Users student = academicRecord.getUser();

            // Navegación US-12: Enrollment -> AcademicRecord (UserDegree) -> Degree -> Name
            String degreeName = (academicRecord != null && academicRecord.getDegree() != null)
                    ? academicRecord.getDegree().getName()
                    : "Carrera no asignada";

            return StudentResponseDTO.builder()
                    .studentId(student.getId())
                    .fullName(student.getFullName())
                    .ci(student.getCi())
                    .email(student.getEmail())
                    .degreeName(degreeName)
                    .build();
        }).toList();
    }

    /**
     * Vista del Estudiante: Obtiene sus materias indicando a qué carrera pertenece la inscripción.
     */
    public java.util.List<MySubjectResponseDTO> getMySubjects(Users currentUser) {
        java.util.List<Enrollment> enrollments = enrollmentRepository.findByAcademicRecord_UserId(currentUser.getId()).stream()
            .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
            .toList();

        return enrollments.stream().map(enrollment -> {
            Subject subject = enrollment.getSubject();
            String teacherName = subject.getTeacher() != null ? subject.getTeacher().getFullName() : "Sin asignar";
            
            // Navegación US-12: Identificar carrera según el expediente de la inscripción
            String degreeName = (enrollment.getAcademicRecord() != null && enrollment.getAcademicRecord().getDegree() != null)
                    ? enrollment.getAcademicRecord().getDegree().getName()
                    : "Carrera no asignada";

            return MySubjectResponseDTO.builder()
                    .subjectCode(subject.getCode())
                    .subjectName(subject.getName())
                    .teacherName(teacherName)
                    .degreeName(degreeName)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public KardexResponse getMyKardex(String userEmail) {
        Users student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado."));

        List<Enrollment> enrollments = enrollmentRepository.findByAcademicRecord_UserId(student.getId());

        if (enrollments.isEmpty()) {
            return KardexResponse.builder()
                    .studentName(student.getFullName())
                    .historyBySemester(Map.of())
                    .build();
        }

        String degreeName = enrollments.get(0).getAcademicRecord().getDegree().getName();

        Map<String, List<SubjectGradeResponse>> history = enrollments.stream()
                .collect(Collectors.groupingBy(
                        e -> "Gestión " + e.getSubject().getSemester().getManagement().getYear() + " - Semestre " + e.getSubject().getSemester().getNumber(),
                        Collectors.mapping(
                                e -> SubjectGradeResponse.builder()
                                        .subjectCode(e.getSubject().getCode())
                                        .subjectName(e.getSubject().getName())
                                        .finalScore(e.getFinalScore())
                                        .status(e.getStatus())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        return KardexResponse.builder()
                .studentName(student.getFullName())
                .degreeName(degreeName)
                .historyBySemester(history)
                .build();
    }

}