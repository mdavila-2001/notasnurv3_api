package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.UserDegree;
import com.universidad_nur.notasnurv3_api.repositories.UserDegreeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDegreeService {

    private final UserDegreeRepository userDegreeRepository;

    /**
     * MÉTODOS DE COMPATIBILIDAD PARA EL CONTROLADOR DE EXPEDIENTES
     * Estos métodos aseguran que la API de gestión de expedientes funcione
     * mientras se implementa la lógica completa.
     */

    @Transactional
    public UserDegreeResponse openRecord(UserDegreeRequest request) {
        // Estructura preparada para la creación de nuevos expedientes académicos
        // Por ahora retorna un DTO vacío para mantener la compatibilidad del Controller
        return UserDegreeResponse.builder().build();
    }

    @Transactional(readOnly = true)
    public List<UserDegreeResponse> getByUserId(UUID userId) {
        // Recupera todos los expedientes (carreras) asociados a un usuario específico
        List<UserDegree> degrees = userDegreeRepository.findByUser_Id(userId);
        
        return degrees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapea la entidad UserDegree a su DTO de respuesta (UserDegreeResponse).
     * Incluye validaciones para evitar NullPointerException en las relaciones.
     */
    private UserDegreeResponse mapToResponse(UserDegree entity) {
        String degreeName = (entity.getDegree() != null) ? entity.getDegree().getName() : "Carrera no asignada";
        
        return UserDegreeResponse.builder()
                .id(entity.getId())
                .degreeName(degreeName)
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .type(entity.getType() != null ? entity.getType().toString() : null)
                .build();
    }
}