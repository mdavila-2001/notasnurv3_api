package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.SemesterRequestDTO;
import com.universidad_nur.notasnurv3_api.dto.SemesterResponseDTO;
import com.universidad_nur.notasnurv3_api.entities.Management;
import com.universidad_nur.notasnurv3_api.entities.Semester;
import com.universidad_nur.notasnurv3_api.exceptions.DuplicateResourceException;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidDateRangeException;
import com.universidad_nur.notasnurv3_api.repositories.ManagementRepository;
import com.universidad_nur.notasnurv3_api.repositories.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final ManagementRepository managementRepository;

    @Transactional
    public SemesterResponseDTO createSemester(SemesterRequestDTO request) {
        if (request.number() < 1 || request.number() > 2) {
            throw new RuntimeException("El número de semestre debe ser 1 o 2.");
        }

        if (!request.endDate().isAfter(request.startDate())) {
            throw new InvalidDateRangeException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        Management management = managementRepository.findById(request.managementId())
                .orElseThrow(() -> new RuntimeException("La gestión no existe."));

        if (semesterRepository.existsByManagementAndNumber(management, request.number())) {
            throw new DuplicateResourceException(
                    "Ya existe el semestre " + request.number() + " para la gestión " + management.getYear() + "."
            );
        }

        Semester semester = Semester.builder()
                .number(request.number())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .management(management)
                .build();

        Semester savedSemester = semesterRepository.save(semester);
        return toResponse(savedSemester);
    }

    @Transactional(readOnly = true)
    public List<SemesterResponseDTO> getAllSemesters() {
        return semesterRepository.findAll(Sort.by(Sort.Direction.ASC, "management.id", "number"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterResponseDTO> getSemestersByManagement(Long managementId) {
        Management management = managementRepository.findById(managementId)
                .orElseThrow(() -> new RuntimeException("La gestión no existe."));

        return semesterRepository.findByManagementId(management.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SemesterResponseDTO toResponse(Semester semester) {
        return new SemesterResponseDTO(
                semester.getId(),
                semester.getNumber(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.getManagement().getId(),
                semester.getManagement().getYear()
        );
    }
}