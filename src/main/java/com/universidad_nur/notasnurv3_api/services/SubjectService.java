package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.stream.Collectors;
import com.universidad_nur.notasnurv3_api.entities.RecordStatus;

import com.universidad_nur.notasnurv3_api.dto.SubjectRequest;
import com.universidad_nur.notasnurv3_api.dto.SubjectResponse;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Subject;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.repositories.SubjectRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import com.universidad_nur.notasnurv3_api.repositories.SemesterRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final GradingService gradingService;

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new RuntimeException("La capacidad de la materia debe ser mayor a 0.");
        }

        Users teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("El usuario con ID " + request.getTeacherId() + " no existe."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new RuntimeException("El usuario asignado no tiene permisos de docente.");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("El semestre seleccionado no existe."));

        Subject.SubjectBuilder subjectBuilder = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacity(request.getCapacity())
                .recordStatus(RecordStatus.DRAFT)
                .semester(semester)
                .teacher(teacher)
                .category(request.getCategory());

        if (request.getModality() != null) {
            subjectBuilder.modality(request.getModality());
        }

        Subject subject = subjectBuilder.build();

        return mapToResponseDTO(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));
        return mapToResponseDTO(subject);
    }

    @Transactional
    public SubjectResponse updateSubject(Integer id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar: Materia no encontrada."));

        subject.setName(request.getName());
        if (request.getModality() != null) {
            subject.setModality(request.getModality());
        }
        subject.setCapacity(request.getCapacity());
        subject.setCategory(request.getCategory());

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new RuntimeException("Semestre no encontrado."));
            subject.setSemester(semester);
        }

        return mapToResponseDTO(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(Integer id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Materia no encontrada.");
        }
        subjectRepository.deleteById(id);
    }

    @Transactional
    public SubjectResponse activateSubject(Integer id) {
        subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada"));
        try {
            subjectRepository.activateSubject(id);
        } catch (Exception e) {
            throw new RuntimeException("Error de validación: Las ponderaciones deben sumar exactamente 100 para activar.");
        }
        Subject activatedSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada después de activar"));
        return mapToResponseDTO(activatedSubject);
    }

    @Transactional
    public SubjectResponse closeSubject(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada."));

        gradingService.calculateFinalGradesForSubject(id);

        subject.setRecordStatus(RecordStatus.CLOSED);
        subjectRepository.save(subject);

        // Generar un log definitivo del acta
        System.out.println("Log de Acta Definitiva: Materia ID " + id + " (" + subject.getName() + ") ha sido CERRADA.");

        return mapToResponseDTO(subject);
    }

    @Transactional
    public void closeSubjectsBySemester(Integer semesterId) {
        List<Subject> subjects = subjectRepository.findAll().stream()
                .filter(s -> s.getSemester().getId().equals(semesterId) && s.getRecordStatus() != RecordStatus.CLOSED)
                .collect(Collectors.toList());

        for (Subject subject : subjects) {
            closeSubject(subject.getId());
        }
    }

    private SubjectResponse mapToResponseDTO(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .modality(subject.getModality())
                .capacity(subject.getCapacity())
                .recordStatus(subject.getRecordStatus())
                .semesterId(subject.getSemester().getId())
                .semesterName("Semestre " + subject.getSemester().getNumber())
                .teacherId(subject.getTeacher().getId())
                .teacherName(subject.getTeacher().getFullName())
                .management(String.valueOf(subject.getSemester().getManagement().getYear()))
                .category(subject.getCategory())
                .build();
    }
}