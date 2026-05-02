package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeRequest;
import com.universidad_nur.notasnurv3_api.dto.UserDegreeResponse;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import com.universidad_nur.notasnurv3_api.services.UserDegreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-degrees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserDegreeController {

    private final UserDegreeService userDegreeService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDegreeResponse>> openRecord(
            @Valid @RequestBody UserDegreeRequest request
    ) {
        UserDegreeResponse response = userDegreeService.openRecord(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Expediente académico abierto", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<UserDegreeResponse>>> getByUserId(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        validateUserCanAccessRecords(userId, authentication);

        List<UserDegreeResponse> response = userDegreeService.getByUserId(userId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Expedientes obtenidos", response)
        );
    }

    private void validateUserCanAccessRecords(UUID requestedUserId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        String loggedUserEmail = authentication.getName();

        Users loggedUser = userRepository.findByEmail(loggedUserEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));

        if (!loggedUser.getId().equals(requestedUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar expedientes de otro usuario"
            );
        }
    }
}