package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.AuthResponse;
import com.universidad_nur.notasnurv3_api.dto.LoginRequest;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.UserStatus;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Users user;
        String identifier = request.id().trim();
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

            if (user.getRole() == Role.STUDENT) {
                throw new RuntimeException("Los estudiantes deben ingresar utilizando su CI, no su correo.");
            }
        } else {
            user = userRepository.findByCi(identifier)
                    .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

            if (user.getRole() != Role.STUDENT) {
                throw new RuntimeException("El personal debe ingresar utilizando su correo institucional.");
            }
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("El usuario se encuentra inactivo en el sistema.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);;

        return new AuthResponse(token, user.getFullName(), user.getRole().name());
    }

}
