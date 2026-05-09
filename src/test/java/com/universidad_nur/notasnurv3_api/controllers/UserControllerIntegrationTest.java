package com.universidad_nur.notasnurv3_api.controllers;

import com.universidad_nur.notasnurv3_api.dto.UserResponse;
import com.universidad_nur.notasnurv3_api.entities.Role;
import com.universidad_nur.notasnurv3_api.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getByRole_debeRetornarListaDeUsuariosSinPassword() throws Exception {
        UserResponse user = buildUserResponse();
        when(userService.getUsersByRole("admin")).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users/role/{roleName}", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuarios obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].email").value("ana@mail.com"))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    void create_debeRetornarCreated() throws Exception {
        doNothing().when(userService).createUser(any());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ci": "123",
                                  "name": "Ana",
                                  "middleName": "Maria",
                                  "lastName": "Perez",
                                  "motherLastName": "Lopez",
                                  "email": "ana@mail.com",
                                  "password": "secreto123",
                                  "role": "TEACHER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario creado correctamente"));

        verify(userService).createUser(any());
    }

    @Test
    void create_debeRetornarBadRequestCuandoFaltanCamposObligatorios() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lastName": "Perez",
                                  "email": "ana@mail.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error en los datos enviados"))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    void update_debeRetornarUsuarioActualizadoSinPassword() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse updated = buildUserResponse();
        when(userService.updateUser(eq(id), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ci": "123",
                                  "name": "Ana",
                                  "middleName": "Maria",
                                  "lastName": "Perez",
                                  "motherLastName": "Lopez",
                                  "email": "ana@mail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actualizado correctamente"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.email").value("ana@mail.com"));
    }

    @Test
    void delete_debeRetornarOk() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario eliminado correctamente"));

        verify(userService).deleteUser(id);
    }

    @Test
    void updateStatus_debeRetornarUsuarioConEstadoActualizado() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse updated = new UserResponse(
                id,
                "123",
                "Ana",
                "Maria",
                "Perez",
                "Lopez",
                "ana@mail.com",
                Role.TEACHER,
                "INACTIVE",
                "Ana Maria Perez Lopez"
        );

        when(userService.updateStatus(id, "INACTIVE")).thenReturn(updated);

        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado actualizado correctamente"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(
                UUID.randomUUID(),
                "123",
                "Ana",
                "Maria",
                "Perez",
                "Lopez",
                "ana@mail.com",
                Role.TEACHER,
                "ACTIVE",
                "Ana Maria Perez Lopez"
        );
    }
}
