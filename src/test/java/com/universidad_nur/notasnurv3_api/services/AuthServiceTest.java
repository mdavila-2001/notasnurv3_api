package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.AuthResponse;
import com.universidad_nur.notasnurv3_api.dto.LoginRequest;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginWithTeacherEmailShouldSucceed() {
        Users teacher = buildUser(Role.TEACHER, "ACTIVE", "teacher@nur.edu.bo", "200", "hashed");

        when(userRepository.findByEmail("teacher@nur.edu.bo")).thenReturn(Optional.of(teacher));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.generateToken(teacher)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("teacher@nur.edu.bo", "secret"));

        assertEquals("jwt-token", response.token());
        assertEquals("TEACHER", response.role());
    }

    @Test
    void studentUsingEmailShouldBeRejected() {
        Users student = buildUser(Role.STUDENT, "ACTIVE", "student@nur.edu.bo", "88997766", "hashed");

        when(userRepository.findByEmail("student@nur.edu.bo")).thenReturn(Optional.of(student));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("student@nur.edu.bo", "secret")));

        assertEquals("Los estudiantes deben ingresar utilizando su CI, no su correo.", ex.getMessage());
    }

    @Test
    void teacherUsingCiShouldBeRejected() {
        Users teacher = buildUser(Role.TEACHER, "ACTIVE", "teacher@nur.edu.bo", "200", "hashed");

        when(userRepository.findByCi("200")).thenReturn(Optional.of(teacher));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(new LoginRequest("200", "secret")));

        assertEquals("El personal debe ingresar utilizando su correo institucional.", ex.getMessage());
    }

    private Users buildUser(Role role, String status, String email, String ci, String password) {
        return Users.builder()
                .name("Nombre")
                .lastName("Apellido")
                .motherLastName("Materno")
                .email(email)
                .ci(ci)
                .password(password)
                .status(status)
                .role(role)
                .build();
    }
}

