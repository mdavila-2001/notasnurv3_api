package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record GradeRequest(
        @NotNull(message = "El ID de la inscripción es requerido")
        UUID enrollmentId,
        
        @NotNull(message = "El ID del componente es requerido")
        Integer componentId,
        
        @NotNull(message = "La nota es requerida")
        @DecimalMin(value = "0.0", message = "La nota no puede ser negativa")
        BigDecimal score
) {}
