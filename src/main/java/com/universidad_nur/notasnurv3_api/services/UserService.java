package com.universidad_nur.notasnurv3_api.services;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.universidad_nur.notasnurv3_api.dto.UserRequest;
import com.universidad_nur.notasnurv3_api.dto.UserResponse;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.UserStatus;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.exceptions.InvalidOperationException;
import com.universidad_nur.notasnurv3_api.exceptions.ResourceNotFoundException;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getUsersByRole(String roleName) {
        Role roleEnum = parseRole(roleName);
        return userRepository.findByRole(roleEnum).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void createUser(UserRequest user) {
        validateRequiredUserFields(user);
        if (isBlank(user.password())) {
            throw new InvalidOperationException("La contraseña es obligatoria para crear el usuario");
        }

        Role role = user.role() == null ? Role.STUDENT : parseRole(user.role());

        userRepository.createNewUser(
            normalizeOptional(user.ci()),
            user.name().trim(),
            normalizeOptional(user.middleName()),
            user.lastName().trim(),
            normalizeOptional(user.motherLastName()),
            user.email().trim().toLowerCase(Locale.ROOT),
            passwordEncoder.encode(user.password().trim()),
            role.name()
        );
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse updateStatus(UUID id, String status) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setStatus(normalizeStatus(status));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest details) {
        validateRequiredUserFields(details);

        Users user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setName(details.name().trim());
        user.setMiddleName(normalizeNullable(details.middleName()));
        user.setLastName(details.lastName().trim());
        user.setMotherLastName(normalizeNullable(details.motherLastName()));
        user.setEmail(details.email().trim().toLowerCase(Locale.ROOT));
        user.setCi(normalizeNullable(details.ci()));

        return toResponse(userRepository.save(user));
    }

    private Role parseRole(String roleName) {
        if (isBlank(roleName)) {
            throw new InvalidOperationException("El rol es obligatorio");
        }

        try {
            return Role.valueOf(roleName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("El rol proporcionado no es válido: " + roleName);
        }
    }

    private UserStatus normalizeStatus(String status) {
        if (isBlank(status)) {
            throw new InvalidOperationException("El estado es obligatorio");
        }

        try {
            return UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Estado inválido. Valores permitidos: ACTIVE, INACTIVE, GRADUATED");
        }
    }

    private void validateRequiredUserFields(UserRequest user) {
        if (user == null || isBlank(user.email()) || isBlank(user.name()) || isBlank(user.lastName())) {
            throw new InvalidOperationException("Nombre, Apellido y Email son obligatorios");
        }
    }

    private UserResponse toResponse(Users user) {
        return new UserResponse(
            user.getId(),
            user.getCi(),
            user.getName(),
            user.getMiddleName(),
            user.getLastName(),
            user.getMotherLastName(),
            user.getEmail(),
            user.getRole(),
            user.getStatus().name(),
            user.getFullName()
        );
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}