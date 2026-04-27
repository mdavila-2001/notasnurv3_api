package com.universidad_nur.notasnurv3_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDegreeService {

    private final UserDegreeRepository userDegreeRepository;
    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public FacultyStatsResponse getFacultyStats(Integer facultyId) {
        // 1. Validar que la facultad existe
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + facultyId + " no fue encontrada."));

        // 2. Contar expedientes con estado ACTIVE en esa facultad
        Long activeCount = userDegreeRepository.countByDegree_Faculty_IdAndStatus(facultyId, AcademicStatus.ACTIVE);

        return FacultyStatsResponse.builder()
                .facultyName(faculty.getName())
                .activeStudentsCount(activeCount)
                .build();
    }

    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {
        // Retornamos un objeto construido con el builder para evitar errores de constructor vacío
        // Joaquín podrá implementar la lógica real aquí más adelante.
        throw new UnsupportedOperationException(
                "La creación de expedientes de usuario aún no está implementada. No se puede abrir un expediente hasta agregar la lógica de persistencia y asignar el estado inicial de activo."
        );
    }

    @Transactional(readOnly = true)
    public List<UserDegreeResponse> getByUserId(UUID userId) {
        // Buscamos los expedientes en el repositorio y los mapeamos al DTO de respuesta
        List<UserDegree> degrees = userDegreeRepository.findByUser_Id(userId);
        
        return degrees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserDegreeResponse mapToResponse(UserDegree entity) {
        String degreeName = (entity.getDegree() != null) ? entity.getDegree().getName() : "Sin carrera";

        String studentName = (entity.getUser() != null) ? entity.getUser().getFullName() : null;

        return UserDegreeResponse.builder()
                .id(entity.getId())
                .studentName(studentName)
                .degreeName(degreeName)
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .type(entity.getType() != null ? entity.getType().toString() : null)
                .build();
    }
}