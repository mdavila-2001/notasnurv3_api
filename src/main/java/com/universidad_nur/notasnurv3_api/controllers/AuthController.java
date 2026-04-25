package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.ApiResponse;
import com.universidad_nur.notasnurv3_api.dto.AuthResponse;
import com.universidad_nur.notasnurv3_api.dto.LoginRequest;
import com.universidad_nur.notasnurv3_api.dto.UserProfileResponse;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.services.AuthService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse tokenData = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Login exitoso", tokenData)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new ApiResponse<>(true, "Sesión cerrada exitosamente. Hasta pronto.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();

        UserProfileResponse profileInfo = new UserProfileResponse(
                user.getId(),
                user.getCi(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Perfil obtenido exitosamente", profileInfo)
        );
    }
}
