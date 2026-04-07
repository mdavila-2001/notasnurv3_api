package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SemesterRequest(
        @NotNull(message = "El número de semestre es obligatorio")
        @Min(value = 1, message = "El número de semestre debe ser 1 o 2")
        @Max(value = 2, message = "El número de semestre debe ser 1 o 2")
        Integer number,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate endDate,

        @NotNull(message = "La gestión es obligatoria")
        Long managementId
) {
}