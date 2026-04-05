package com.universidad_nur.notasnurv3_api.services;

// 1. IMPORTANTE: Agregar el import de List
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
        // Validación de Cupo
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new RuntimeException("La capacidad de la materia debe ser mayor a 0.");
        }

        Users teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("El docente no existe."));

        if (teacher.getRole() == null || !teacher.getRole().getName().equalsIgnoreCase("TEACHER")) {
            throw new RuntimeException("El usuario asignado debe tener el rol de TEACHER.");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new RuntimeException("El semestre seleccionado no existe."));


        Subject subject = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .modality(request.getModality())
                .capacity(request.getCapacity())
                .recordStatus("ACTIVE")
                .semester(semester) 
                .teacher(teacher)
                .build();

        return subjectRepository.save(subject);
    }

    // 5. El método que necesitaba java.util.List
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
}