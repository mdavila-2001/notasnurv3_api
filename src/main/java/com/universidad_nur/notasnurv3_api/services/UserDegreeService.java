package com.universidad_nur.notasnurv3_api.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDegreeService {

    private final UserDegreeRepository userDegreeRepository;

    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {
        // Mantenemos el error controlado que subieron a dev para indicar que no está listo
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "La creación de expedientes de usuario aún no está implementada. No se puede abrir un expediente hasta agregar la lógica de persistencia."
        );
    }

    @Transactional(readOnly = true)
    public List<UserDegreeResponse> getByUserId(UUID userId) {
        List<UserDegree> degrees = userDegreeRepository.findByUser_Id(userId);
        
        return degrees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserDegreeResponse mapToResponse(UserDegree entity) {
        // Consolidamos las validaciones de ambos mundos
        String degreeName = (entity.getDegree() != null) ? entity.getDegree().getName() : "Sin carrera";
        String studentName = (entity.getUser() != null) ? entity.getUser().getFullName() : null;

        return UserDegreeResponse.builder()
                .id(entity.getId())
                .studentName(studentName)
                .degreeName(degreeName)
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .type(entity.getType() != null ? entity.getType().toString() : null)
                .build();
    }
}