package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.ManagementRequestDTO;
import com.universidad_nur.notasnurv3_api.dto.ManagementResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
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
    public ManagementResponseDTO createManagement(ManagementRequestDTO request) {
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
    public List<ManagementResponseDTO> getAllManagements() {
        return managementRepository.findAll(Sort.by(Sort.Direction.ASC, "year"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteManagement(Long id) {
        Management management = managementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La gestión no existe."));

        if (management.getSemesters() != null && !management.getSemesters().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la gestión porque tiene semestres asociados.");
        }

        managementRepository.delete(management);
    }

    private ManagementResponseDTO toResponse(Management management) {
        return new ManagementResponseDTO(management.getId(), management.getYear());
    }
}