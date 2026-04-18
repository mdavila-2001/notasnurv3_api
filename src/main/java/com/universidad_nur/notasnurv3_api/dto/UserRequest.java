package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        String ci,

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        String middleName,

        @NotBlank(message = "El apellido paterno es obligatorio")
        String lastName,

        String motherLastName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        String password,
        String role
) {
}
