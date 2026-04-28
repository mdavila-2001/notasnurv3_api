package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.FacultyRequest;
import com.universidad_nur.notasnurv3_api.dto.FacultyResponse;
import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.DegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DegreeRepository degreeRepository;
    private final UserDegreeRepository userDegreeRepository;

    /**
     * Obtener todas las facultades
     */
    @Transactional(readOnly = true)
    public List<FacultyResponse> getAll() {
        return facultyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtener una facultad por ID
     */
    @Transactional(readOnly = true)
    public FacultyResponse getById(Integer id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + id + " no fue encontrada."));
        return mapToResponse(faculty);
    }

    /**
     * Crear una nueva facultad
     */
    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        // Validar que el código no exista
        if (facultyRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ya existe una facultad con el código: " + request.code());
        }

        // Validar que el nombre no exista
        if (facultyRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Ya existe una facultad con el nombre: " + request.name());
        }

        Faculty faculty = Faculty.builder()
                .name(request.name())
                .code(request.code())
                .build();

        Faculty saved = facultyRepository.save(faculty);
        return mapToResponse(saved);
    }

    /**
     * Actualizar una facultad existente
     */
    @Transactional
    public FacultyResponse update(Integer id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + id + " no fue encontrada."));

        // Validar que el código no esté duplicado (excepto el mismo registro)
        if (!faculty.getCode().equals(request.code()) && facultyRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Ya existe otra facultad con el código: " + request.code());
        }

        // Validar que el nombre no esté duplicado (excepto el mismo registro)
        if (!faculty.getName().equals(request.name()) && facultyRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Ya existe otra facultad con el nombre: " + request.name());
        }

        faculty.setName(request.name());
        faculty.setCode(request.code());

        Faculty updated = facultyRepository.save(faculty);
        return mapToResponse(updated);
    }

    /**
     * Eliminar una facultad (solo si no tiene carreras asociadas)
     */
    @Transactional
    public void delete(Integer id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + id + " no fue encontrada."));

        // Validar que no existan carreras asociadas
        long degreeCount = degreeRepository.countByFacultyId(id);
        if (degreeCount > 0) {
            throw new InvalidOperationException("No se puede eliminar la facultad porque tiene " + degreeCount + " carrera(s) asociada(s).");
        }

        facultyRepository.delete(faculty);
    }

    /**
     * Obtener estadísticas de una facultad (alumnos activos)
     */
    @Transactional(readOnly = true)
    public FacultyStatsResponse getStats(Integer facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + facultyId + " no fue encontrada."));

        Long activeCount = userDegreeRepository.countByDegree_Faculty_IdAndStatus(facultyId, AcademicStatus.ACTIVE);

        return FacultyStatsResponse.builder()
                .facultyName(faculty.getName())
                .activeStudentsCount(activeCount)
                .build();
    }

    /**
     * Mapear entidad Faculty a DTO FacultyResponse
     */
    private FacultyResponse mapToResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getName(),
                faculty.getCode(),
                faculty.getCreatedAt(),
                faculty.getUpdatedAt()
        );
    }
}