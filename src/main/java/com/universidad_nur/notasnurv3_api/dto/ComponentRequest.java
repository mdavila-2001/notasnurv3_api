package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ComponentRequest(
        @NotBlank(message = "El nombre del componente es obligatorio")
        String name,

        @NotNull(message = "El peso (ponderación) es obligatorio")
        @Min(value = 1, message = "El peso debe ser al menos 1")
        @Max(value = 100, message = "El peso no puede exceder 100")
        BigDecimal weight,

        String description,

        @NotNull(message = "El ID del plan de evaluación es obligatorio")
        UUID planId
) {
}
