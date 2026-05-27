package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
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
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;
    private final GradingService gradingService;

    @Autowired
    @Lazy
    private SubjectService self;

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new InvalidOperationException("La capacidad de la materia debe ser mayor a 0.");
        }

        Users teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario con ID " + request.getTeacherId() + " no existe."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new InvalidOperationException("El usuario asignado no tiene permisos de docente.");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("El semestre seleccionado no existe."));

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
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SubjectResponse> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAllWithAssociations(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getMySubjects(Users currentUser) {
        return subjectRepository.findByTeacher_Id(currentUser.getId()).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada con ID: " + id));
        return mapToResponseDTO(subject);
    }

    @Transactional
    public SubjectResponse updateSubject(Integer id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar: Materia no encontrada."));

        subject.setName(request.getName());
        if (request.getModality() != null) {
            subject.setModality(request.getModality());
        }
        subject.setCapacity(request.getCapacity());
        subject.setCategory(request.getCategory());

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Semestre no encontrado."));
            subject.setSemester(semester);
        }
        if (request.getRecordStatus() != null) {
    subject.setRecordStatus(request.getRecordStatus());
}

        return mapToResponseDTO(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(Integer id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: Materia no encontrada.");
        }
        subjectRepository.deleteById(id);
    }

    @Transactional
    public SubjectResponse activateSubject(Integer id) {
        subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada"));
        try {
            subjectRepository.activateSubject(id);
        } catch (Exception e) {
            throw new InvalidOperationException("Error de validación: Las ponderaciones deben sumar exactamente 100 para activar.");
        }
        Subject activatedSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada después de activar"));
        return mapToResponseDTO(activatedSubject);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public SubjectResponse closeSubject(Integer id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada."));

        gradingService.calculateFinalGradesForSubject(id);

        subject.setRecordStatus(RecordStatus.CLOSED);
        subjectRepository.save(subject);

        // Generar un log definitivo del acta
        log.info("Log de Acta Definitiva: Materia ID {} ({}) ha sido CERRADA.", id, subject.getName());

        return mapToResponseDTO(subject);
    }

    @Transactional
    public void closeSubjectsBySemester(Integer semesterId) {
        // Filtrar a nivel de DB directamente en lugar de traer todo a memoria
        List<Subject> subjects = subjectRepository.findBySemesterIdAndRecordStatusNot(semesterId, RecordStatus.CLOSED);

        for (Subject subject : subjects) {
            try {
                self.closeSubject(subject.getId());
            } catch (Exception e) {
                log.error("Fallo al cerrar materia ID {}: {}. Continuando con las siguientes materias del semestre.", 
                        subject.getId(), e.getMessage());
            }
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