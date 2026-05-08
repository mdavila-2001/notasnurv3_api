package com.universidad_nur.notasnurv3_api.services;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional(readOnly = true)
    public FacultyStatsResponse getFacultyStats(Integer facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("La facultad no fue encontrada"));

        Long activeCount = userDegreeRepository.countByDegree_Faculty_IdAndStatus(facultyId, AcademicStatus.ACTIVE);

        return FacultyStatsResponse.builder()
                .facultyName(faculty.getName())
                .activeStudentsCount(activeCount)
                .build();
    }

    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {

        Users user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Degree degree = degreeRepository.findById(request.degreeId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada"));

        boolean exists = userDegreeRepository.existsByUser_IdAndDegree_IdAndStatus(
                request.userId(),
                request.degreeId(),
                AcademicStatus.ACTIVE
        );

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El usuario ya tiene un expediente activo en esta carrera.");
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
        return userDegreeRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserDegreeResponse mapToResponse(UserDegree entity) {
        return UserDegreeResponse.builder()
                .id(entity.getId())
                .studentName(entity.getUser().getFullName())
                .degreeName(entity.getDegree().getName())
                .type(entity.getType().toString())
                .status(entity.getStatus().toString())
                .build();
    }
}