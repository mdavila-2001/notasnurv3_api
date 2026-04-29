package com.universidad_nur.notasnurv3_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserDegreeRepository userDegreeRepository;

    @Transactional(readOnly = true)
    public FacultyStatsResponse getStats(Integer facultyId) {
        // 1. Validar que la facultad existe
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + facultyId));

        // 2. Contar expedientes con estado ACTIVE en esa facultad
        long count = userDegreeRepository.countByDegree_Faculty_IdAndStatus(facultyId, AcademicStatus.ACTIVE);

        // 3. Retornar respuesta completa (incluyendo ID como está en dev)
        return FacultyStatsResponse.builder()
                .facultyId(faculty.getId())
                .facultyName(faculty.getName())
                .activeStudentsCount(count)
                .build();
    }
}