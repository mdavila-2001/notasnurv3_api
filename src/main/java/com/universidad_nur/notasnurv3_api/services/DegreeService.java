package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.DegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.DegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.Degree;
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
public class DegreeService {

    private final DegreeRepository degreeRepository;
    private final FacultyRepository facultyRepository;
    private final UserDegreeRepository userDegreeRepository;

    /**
     * Obtener todas las carreras con información de facultad
     */
    @Transactional(readOnly = true)
    public List<DegreeResponse> getAll() {
        return degreeRepository.findAllWithFaculty()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtener una carrera por ID
     */
    @Transactional(readOnly = true)
    public DegreeResponse getById(Integer id) {
        Degree degree = degreeRepository.findByIdWithFaculty(id)
                .orElseThrow(() -> new ResourceNotFoundException("La carrera con ID " + id + " no fue encontrada."));
        return mapToResponse(degree);
    }

    /**
     * Crear una nueva carrera
     */
    @Transactional
    public DegreeResponse create(DegreeRequest request) {
        // Validar que la facultad exista
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + request.facultyId() + " no fue encontrada."));

        // Validar que el código no exista
        if (degreeRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ya existe una carrera con el código: " + request.code());
        }

        // Validar que el nombre no exista
        if (degreeRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Ya existe una carrera con el nombre: " + request.name());
        }

        Degree degree = Degree.builder()
                .name(request.name())
                .code(request.code())
                .faculty(faculty)
                .build();

        Degree saved = degreeRepository.save(degree);
        return mapToResponse(saved);
    }

    /**
     * Actualizar una carrera existente
     */
    @Transactional
    public DegreeResponse update(Integer id, DegreeRequest request) {
        Degree degree = degreeRepository.findByIdWithFaculty(id)
                .orElseThrow(() -> new ResourceNotFoundException("La carrera con ID " + id + " no fue encontrada."));

        // Validar que la nueva facultad exista (si cambia)
        Faculty faculty = degree.getFaculty();
        if (!degree.getFaculty().getId().equals(request.facultyId())) {
            faculty = facultyRepository.findById(request.facultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + request.facultyId() + " no fue encontrada."));
        }

        // Validar que el código no esté duplicado (excepto el mismo registro)
        if (!degree.getCode().equals(request.code()) && degreeRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Ya existe otra carrera con el código: " + request.code());
        }

        // Validar que el nombre no esté duplicado (excepto el mismo registro)
        if (!degree.getName().equals(request.name()) && degreeRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Ya existe otra carrera con el nombre: " + request.name());
        }

        degree.setName(request.name());
        degree.setCode(request.code());
        degree.setFaculty(faculty);

        Degree updated = degreeRepository.save(degree);
        return mapToResponse(updated);
    }

    /**
     * Eliminar una carrera (solo si no tiene inscripciones)
     */
    @Transactional
    public void delete(Integer id) {
        Degree degree = degreeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La carrera con ID " + id + " no fue encontrada."));

        // Validar que no existan expedientes académicos (UserDegree) asociados
        long userDegreeCount = userDegreeRepository.countByDegree_Id(id);
        if (userDegreeCount > 0) {
            throw new InvalidOperationException("No se puede eliminar la carrera porque tiene " + userDegreeCount + " expediente(s) académico(s) asociado(s).");
        }

        degreeRepository.delete(degree);
    }

    /**
     * Mapear entidad Degree a DTO DegreeResponse
     */
    private DegreeResponse mapToResponse(Degree degree) {
        return new DegreeResponse(
                degree.getId(),
                degree.getName(),
                degree.getCode(),
                degree.getFaculty().getId(),
                degree.getFaculty().getName(),
                degree.getCreatedAt(),
                degree.getUpdatedAt()
        );
    }
}