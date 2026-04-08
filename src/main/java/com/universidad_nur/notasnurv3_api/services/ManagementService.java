package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ManagementRequest;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponse;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final ManagementRepository managementRepository;

    @Transactional
    public ManagementResponse createManagement(ManagementRequest request) {
        if (managementRepository.existsByYear(request.year())) {
            throw new DuplicateResourceException("Ya existe una gestión registrada para el año " + request.year() + ".");
        }

        Management management = Management.builder()
                .year(request.year())
                .build();

        Management savedManagement = managementRepository.save(management);
        return toResponse(savedManagement);
    }

    @Transactional(readOnly = true)
    public List<ManagementResponse> getAllManagements() {
        return managementRepository.findAll(Sort.by(Sort.Direction.ASC, "year"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManagementResponse getById(Integer id) {
        return managementRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con id: " + id));
    }

    @Transactional
    public ManagementResponse update(Integer id, ManagementRequest request) {
        Management management = managementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con id: " + id));

        // Validar si el nuevo año ya existe en otra gestión
        if (!management.getYear().equals(request.year()) &&
                managementRepository.existsByYear(request.year())) {
            throw new DuplicateResourceException("El año " + request.year() + " ya está registrado");
        }

        management.setYear(request.year());
        return toResponse(managementRepository.save(management));
    }

    @Transactional
    public void deleteManagement(Integer id) {
        Management management = managementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La gestión no existe."));

        if (management.getSemesters() != null && !management.getSemesters().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la gestión porque tiene semestres asociados.");
        }

        managementRepository.delete(management);
    }

    private ManagementResponse toResponse(Management management) {
        return new ManagementResponse(management.getId(), management.getYear());
    }
}