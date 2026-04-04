package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "La credencial o correo son obligatorios") String id,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {
}
