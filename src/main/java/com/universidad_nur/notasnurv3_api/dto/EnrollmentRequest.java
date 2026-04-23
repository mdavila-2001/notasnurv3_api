package com.universidad_nur.notasnurv3_api.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "El ID del expediente académico es obligatorio")
        Integer userDegreeId,

        @NotNull(message = "El ID de la materia es obligatorio")
        Integer subjectId
) {
}
