package com.universidad_nur.notasnurv3_api.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.universidad_nur.notasnurv3_api.dto.FacultyStatsResponse;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.AcademicStatus;
import com.universidad_nur.notasnurv3_api.entities.Degree;
import com.universidad_nur.notasnurv3_api.entities.Faculty;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.DegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.FacultyRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDegreeService {

    private final UserDegreeRepository userDegreeRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final DegreeRepository degreeRepository;

    /**
     * US-12: Obtener estadísticas de alumnos activos por facultad.
     */
    @Transactional(readOnly = true)
    public FacultyStatsResponse getFacultyStats(Integer facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad con ID " + facultyId + " no fue encontrada."));

        Long activeCount = userDegreeRepository.countByDegree_Faculty_IdAndStatus(facultyId, AcademicStatus.ACTIVE);

        return FacultyStatsResponse.builder()
                .facultyName(faculty.getName())
                .activeStudentsCount(activeCount)
                .build();
    }

    /**
     * US-11: Apertura de expediente académico.
     */
    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {

        Users user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario con ID " + request.userId() + " no fue encontrado."
                ));

        Degree degree = degreeRepository.findById(request.degreeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La carrera con ID " + request.degreeId() + " no fue encontrada."
                ));

        boolean exists = userDegreeRepository.existsByUser_IdAndDegree_IdAndStatus(
                request.userId(),
                request.degreeId(),
                AcademicStatus.ACTIVE
        );

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya tiene un expediente activo en esta carrera."
            );
        }

        UserDegree userDegree = UserDegree.builder()
                .user(user)
                .degree(degree)
                .type(request.type())
                .status(AcademicStatus.ACTIVE)
                .build();

        UserDegree saved = userDegreeRepository.save(userDegree);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDegreeResponse> getByUserId(UUID userId) {
        List<UserDegree> degrees = userDegreeRepository.findByUser_Id(userId);

        return degrees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserDegreeResponse mapToResponse(UserDegree entity) {
        String degreeName = (entity.getDegree() != null) ? entity.getDegree().getName() : "Sin carrera";
        String studentName = (entity.getUser() != null) ? entity.getUser().getFullName() : "Sin estudiante";

        return UserDegreeResponse.builder()
                .id(entity.getId())
                .studentName(studentName)
                .degreeName(degreeName)
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .type(entity.getType() != null ? entity.getType().toString() : null)
                .build();
    }
}