package com.universidad_nur.notasnurv3_api.services;

import com.universidad_nur.notasnurv3_api.dto.UserRequest;
import com.universidad_nur.notasnurv3_api.dto.UserResponse;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.entities.Users;
import com.universidad_nur.notasnurv3_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsersByRole_debeRetornarUsuariosCuandoRolEsValido() {
        Users user = buildUser(Role.ADMIN);
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(user));

        List<UserResponse> response = userService.getUsersByRole("admin");

        assertEquals(1, response.size());
        assertEquals(Role.ADMIN, response.getFirst().role());
        verify(userRepository).findByRole(Role.ADMIN);
    }

    @Test
    void getUsersByRole_debeLanzarExcepcionCuandoRolEsInvalido() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUsersByRole("bad-role"));

        assertEquals("El rol proporcionado no es válido: bad-role", exception.getMessage());
        verify(userRepository, never()).findByRole(any(Role.class));
    }

    @Test
    void createUser_debeCodificarContrasenaYGuardarPorProcedimiento() {
        UserRequest request = new UserRequest(
                "123",
                "Ana",
                "Maria",
                "Perez",
                "Lopez",
                "ANA@MAIL.COM",
                "secreto123",
                "teacher"
        );
        when(passwordEncoder.encode("secreto123")).thenReturn("encoded-pass");

        userService.createUser(request);

        verify(passwordEncoder).encode("secreto123");
        verify(userRepository).createNewUser(
                eq("123"),
                eq("Ana"),
                eq("Maria"),
                eq("Perez"),
                eq("Lopez"),
                eq("ana@mail.com"),
                eq("encoded-pass"),
                eq("TEACHER")
        );
    }

    @Test
    void createUser_debeLanzarExcepcionCuandoContrasenaNoEsProvista() {
        UserRequest request = new UserRequest(
                "123",
                "Ana",
                null,
                "Perez",
                null,
                "ana@mail.com",
                "  ",
                null
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));

        assertEquals("La contraseña es obligatoria para crear el usuario", exception.getMessage());
        verify(userRepository, never()).createNewUser(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteUser_debeEliminarCuandoUsuarioExiste() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_debeLanzarExcepcionCuandoUsuarioNoExiste() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(id));

        assertEquals("No se puede eliminar: Usuario no encontrado", exception.getMessage());
        verify(userRepository, never()).deleteById(id);
    }

    @Test
    void updateStatus_debeNormalizarYGuardarCuandoStatusEsValido() {
        UUID id = UUID.randomUUID();
        Users user = buildUser(Role.STUDENT);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateStatus(id, " graduated ");

        assertEquals("GRADUATED", user.getStatus());
        assertEquals("GRADUATED", response.status());
        verify(userRepository).save(user);
    }

    @Test
    void updateStatus_debeLanzarExcepcionCuandoStatusEsInvalido() {
        UUID id = UUID.randomUUID();
        Users user = buildUser(Role.STUDENT);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateStatus(id, "enabled"));

        assertEquals("Estado inválido. Valores permitidos: ACTIVE, INACTIVE, GRADUATED", exception.getMessage());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void updateUser_debeActualizarCamposEditables() {
        UUID id = UUID.randomUUID();
        Users existing = buildUser(Role.STUDENT);
        existing.setCi("100");
        existing.setFacultad("Ingenieria");

        UserRequest request = new UserRequest(
                "200",
                "  Carla  ",
                "  Beatriz ",
                "  Rojas ",
                " Diaz ",
                " CARLA@MAIL.COM ",
                null,
                null
        );

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse response = userService.updateUser(id, request);

        assertEquals("Carla", response.name());
        assertEquals("Beatriz", response.middleName());
        assertEquals("Rojas", response.lastName());
        assertEquals("Diaz", response.motherLastName());
        assertEquals("carla@mail.com", response.email());
        assertEquals("200", response.ci());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_debeLanzarExcepcionCuandoUsuarioNoExiste() {
        UUID id = UUID.randomUUID();
        UserRequest request = new UserRequest("200", "Carla", null, "Rojas", null, "carla@mail.com", null, null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(id, request));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, never()).save(any(Users.class));
    }

    private Users buildUser(Role role) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setName("Juan");
        user.setMiddleName("Carlos");
        user.setLastName("Perez");
        user.setMotherLastName("Lopez");
        user.setEmail("juan@mail.com");
        user.setRole(role);
        user.setStatus("ACTIVE");
        return user;
    }
}
