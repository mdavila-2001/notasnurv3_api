package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ManagementRequestDTO(
        @NotNull(message = "El año de la gestión es obligatorio")
        @Min(value = 2000, message = "El año de la gestión debe ser mayor o igual a 2000")
        Integer year
) {
}