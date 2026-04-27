package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserDegreeRepository userDegreeRepository;

    @Transactional(readOnly = true)
    public FacultyStatsResponse getStats(Integer facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + facultyId));

        long count = userDegreeRepository.countByDegree_FacultyIdAndStatus(facultyId, AcademicStatus.ACTIVE);

        return new FacultyStatsResponse(
                faculty.getId(),
                faculty.getName(),
                count
        );
    }
}
