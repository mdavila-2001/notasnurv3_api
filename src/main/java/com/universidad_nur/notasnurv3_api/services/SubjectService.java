package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.UUID;

import com.universidad_nur.notasnurv3_api.dto.SubjectRequest;
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


    @Transactional
    public Subject createSubject(SubjectRequest request) {

        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new RuntimeException("La capacidad de la materia debe ser mayor a 0.");
        }

    
        Users teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("El usuario con ID " + request.getTeacherId() + " no existe."));

        if (teacher.getRole() == null || !"TEACHER".equalsIgnoreCase(teacher.getRole())) {
            throw new RuntimeException("El usuario asignado no tiene permisos de docente (Rol incorrecto).");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("El semestre seleccionado no existe."));


        Subject subject = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .modality(request.getModality())
                .capacity(request.getCapacity())
                .recordStatus("DRAFT")
                .semester(semester)
                .teacher(teacher)
                .build();

        return subjectRepository.save(subject);
    }


    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Subject getSubjectById(Integer id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));
    }

    @Transactional
    public Subject updateSubject(Integer id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar: Materia no encontrada."));

        subject.setName(request.getName());
        subject.setModality(request.getModality());
        subject.setCapacity(request.getCapacity());

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new RuntimeException("Semestre no encontrado."));
            subject.setSemester(semester);
        }

        return subjectRepository.save(subject);
    }


    @Transactional
    public void deleteSubject(Integer id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Materia no encontrada.");
        }

        subjectRepository.deleteById(id);
    }
}