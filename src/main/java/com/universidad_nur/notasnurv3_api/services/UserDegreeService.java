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

    /**
     * US-12: Obtener estadísticas de alumnos activos por facultad.
     */
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

    /**
     * MÉTODOS DE COMPATIBILIDAD PARA EL CONTROLADOR
     * Estos métodos permiten que UserDegreeController compile correctamente.
     */

    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {
        // Retornamos un objeto construido con el builder para evitar errores de constructor vacío
        // Joaquín podrá implementar la lógica real aquí más adelante.
        return UserDegreeResponse.builder().build();
    }

    @Transactional(readOnly = true)
    public List<UserDegreeResponse> getByUserId(UUID userId) {
        // Buscamos los expedientes en el repositorio y los mapeamos al DTO de respuesta
        List<UserDegree> degrees = userDegreeRepository.findByUser_Id(userId);
        
        return degrees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapea la entidad UserDegree a su DTO de respuesta.
     */
    private UserDegreeResponse mapToResponse(UserDegree entity) {
        // Validación de seguridad para evitar NullPointerException en las relaciones
        String degreeName = (entity.getDegree() != null) ? entity.getDegree().getName() : "Sin carrera";
        
        return UserDegreeResponse.builder()
                .id(entity.getId())
                .degreeName(degreeName)
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .type(entity.getType() != null ? entity.getType().toString() : null)
                .build();
    }
}